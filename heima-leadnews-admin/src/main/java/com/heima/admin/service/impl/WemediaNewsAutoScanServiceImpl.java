package com.heima.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.feign.ArticleFeign;
import com.heima.admin.feign.WemediaFeign;
import com.heima.admin.mapper.AdChannelMapper;
import com.heima.admin.mapper.AdSensitiveMapper;
import com.heima.admin.service.WemediaNewsAutoScanService;
import com.heima.common.aliyun.GreeTextScan;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.fastdfs.FastDFSClientUtil;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WemediaNewsAutoScanServiceImpl implements WemediaNewsAutoScanService {

    @Autowired
    private WemediaFeign wemediaFeign;

    //1.自动审核 status = 1 kafka监听
    public void autoScanByMediaNewsId2(Integer id) {
        //需要将文章中的文本和图片，抽取出来，才能做审核
    }

    //2.人工审核(审核成功) status= 3 admin端页面上进行操作之后触发
    public void manualScan(Integer id) {

    }

    //3.发布 status= 8 定时任务扫描，进行触发
    public void publish(Integer id) {

    }

    @GlobalTransactional
    @Override
    public void autoScanByMediaNewsId(Integer id) {
        if (id == null) {
            log.error("当前的审核id空");
            return;
        }
        //1.根据id查询自媒体文章信息
        WmNews wmNews = wemediaFeign.findById(id);
        if (wmNews == null) {
            log.error("审核的自媒体文章不存在，自媒体的id:{}", id);
            return;
        }

        //这两步，不是通过kafka触发的 不属于自动审核
        //2.文章状态为4（人工审核通过）直接保存数据和创建索引
        if (wmNews.getStatus() == 4) {
            //保存数据
            saveAppArticle(wmNews);
            return;
        }

        //3.文章状态为8  发布时间<=当前时间 直接保存数据
        //定时任务触发
        if (wmNews.getStatus() == 8 && wmNews.getPublishTime().getTime() <= System.currentTimeMillis()) {
            //保存数据
            saveAppArticle(wmNews);
            return;
        }

        //4.文章状态为1，待审核
        if (wmNews.getStatus() == 1) {
            //抽取文章内容中的纯文本和图片 JAVA不支持多个返回值 python
            Map<String, Object> contentAndImagesResult = handleTextAndImages(wmNews);
            //4.1 文本审核
            boolean textScanBoolean = handleTextScan((String) contentAndImagesResult.get("content"), wmNews);
            if (!textScanBoolean) return;
            //4.2 图片审核
            boolean imagesScanBoolean = handleImagesScan((List<String>) contentAndImagesResult.get("images"), wmNews);
            if (!imagesScanBoolean) return;
            //4.3 自管理的敏感词审核
            boolean sensitiveScanBoolean = handleSensitive((String) contentAndImagesResult.get("content"), wmNews);
            if (!sensitiveScanBoolean) return;
            //4.4 发布时间大于当前时间，
            if (wmNews.getPublishTime().getTime() > System.currentTimeMillis()) {
                //修改文章状态为8
                updateWmNews(wmNews, (short) 8, "审核通过，待发布");
                return;
            }
            //5.审核通过，修改自媒体文章状态为9  保存app端相关文章信息
            saveAppArticle(wmNews);
            //int i = 1/0;
        }


    }

    @Autowired
    private AdSensitiveMapper adSensitiveMapper;

    /**
     * 敏感词审核
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitive(String content, WmNews wmNews) {

        boolean flag = true;

        if (SensitiveWordUtil.needToInit()) {
            List<String> allSensitive = adSensitiveMapper.findAllSensitive();
            //初始化敏感词，将敏感词放到静态map中
            SensitiveWordUtil.initMap(allSensitive);
        }
        //文章内容自管理敏感词过滤
        Map<String, Integer> resultMap = SensitiveWordUtil.matchWords(content);
        if (resultMap.size() > 0) {
            log.error("敏感词过滤没有通过，包含了敏感词:{}", resultMap);
            //找到了敏感词，审核不通过
            updateWmNews(wmNews, (short) 2, "文章中包含了敏感词");
            flag = false;
        }

        return flag;
    }

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FastDFSClientUtil fastDFSClient;

    @Value("${fdfs.url}")
    private String fileServerUrl;

    /**
     * 审核图片
     *
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImagesScan(List<String> images, WmNews wmNews) {
        //判断图片是否为空
        if (CollectionUtils.isEmpty(images)) {
            return true;
        }

        boolean flag = true;

        List<byte[]> imageList = new ArrayList<>();

        try {
            for (String image : images) {
                //去除前缀 http://192.168.226.134:8888/
                String imageName = image.replace(fileServerUrl, "");
                //找到第一个斜杠 group1/M00/00/00/rBENvl1RF0GAIKuTAAE4r64gbnE179.jpg
                int index = imageName.indexOf("/");
                //从0到第一个斜杠（不包含斜杠） group1
                String groupName = imageName.substring(0, index);
                //从第一个斜杠后一位到最后 M00/00/00/rBENvl1RF0GAIKuTAAE4r64gbnE179.jpg
                String imagePath = imageName.substring(index + 1);
                //通过fastdfs下载文件，保存到内容中byte数组
                byte[] imageByte = fastDFSClient.download(groupName, imagePath);
                //将byte数组放到list中，方便提交到阿里云
                imageList.add(imageByte);
            }
            //阿里云图片审核
            Map map = greenImageScan.imageScan(imageList);
            //审核不通过
//            if (!map.get("suggestion").equals("pass")) {
//                //审核失败
//                if (map.get("suggestion").equals("block")) {
//                    //修改自媒体文章的状态，并告知审核失败原因
//                    updateWmNews(wmNews, (short) 2, "文章中图片有违规");
//                    flag = false;
//                }
//
//                //人工审核
//                if (map.get("suggestion").equals("review")) {
//                    //修改自媒体文章的状态，并告知审核失败原因
//                    updateWmNews(wmNews, (short) 3, "文章图片有不确定元素");
//                    flag = false;
//                }
//            }

            String suggestion = (String) map.get("suggestion");

            //审核失败
            if ("block".equals(suggestion)) {
                //修改自媒体文章的状态，并告知审核失败原因
                updateWmNews(wmNews, (short) 2, "文章中图片有违规");
                flag = false;
            }else if("review".equals(suggestion)){
                //修改自媒体文章的状态，并告知审核失败原因
                updateWmNews(wmNews, (short) 3, "文章图片有不确定元素");
                flag = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            updateWmNews(wmNews, (short) 3, "文章图片有不确定元素");
            flag = false;
        }
        return flag;

    }


    @Autowired
    private GreeTextScan greeTextScan;

    /**
     * 文本审核
     *
     * @param content 文本内容
     * @param wmNews  文章对象
     * @return 是否成功标识 true or false
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        //审核结果，是否成功
        boolean flag = true;
        try {
            //使用阿里云进行审核
            Map map = greeTextScan.greeTextScan(content);
            //返回结果中的suggestion字段存放了结果
            //审核不通过
            //审核失败
//
//            if (!map.get("suggestion").equals("pass")) {
//                //审核失败
//                if (map.get("suggestion").equals("block")) {
//                    //修改自媒体文章的状态，并告知审核失败原因
//                    updateWmNews(wmNews, (short) 2, "文章内容中有敏感词汇");
//                    flag = false;
//                }
//
//                //人工审核
//                if (map.get("suggestion").equals("review")) {
//                    //修改自媒体文章的状态，并告知审核失败原因
//                    updateWmNews(wmNews, (short) 3, "文章内容中有不确定词汇");
//                    flag = false;
//                }
//            }

            String suggestion = (String) map.get("suggestion");

            if ("block".equals(suggestion)) {
                //修改自媒体文章的状态，并告知审核失败原因
                updateWmNews(wmNews, (short) 2, "文章内容中有敏感词汇");
                flag = false;
            } else if (suggestion.equals("review")) {
                //修改自媒体文章的状态，并告知审核失败原因
                updateWmNews(wmNews, (short) 3, "文章内容中有不确定词汇");
                flag = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
            updateWmNews(wmNews, (short) 3, "自动审核调用失败，需要人工审核");
            flag = false;
        }

        return flag;
    }

    /**
     * 修改自媒体文章
     *
     * @param wmNews
     * @param status
     * @param msg
     */
    private void updateWmNews(WmNews wmNews, short status, String msg) {
        wmNews.setStatus(status);
        wmNews.setReason(msg);
        wemediaFeign.updateWmNews(wmNews);
    }

    /**
     * 提取文本内容和图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        //文章的内容
        String content = wmNews.getContent();

        //存储纯文本内容
        StringBuilder sb = new StringBuilder();
        // a  b c d -> String
        //存储图片
        Set<String> images = new HashSet<>();

        //content是一个JSON数组，如下
        /**
         [
         *     {
         *         "type":"text",
         *         "value":"随着智能手机的普及，人们更加习惯于通过手机来看新闻。由于生活节奏的加快，很多人只能利用碎片时间来获取信息，因此，对于移动资讯客户端的需求也越来越高。黑马头条项目正是在这样背景下开发出来。黑马头条项目采用当下火热的微服务+大数据技术架构实现。本项目主要着手于获取最新最热新闻资讯，通过大数据分析用户喜好精确推送咨询新闻"
         *     },
         *     {
         *         "type":"image",
         *         "value":"http://192.168.200.130/group1/M00/00/00/wKjIgl5swbGATaSAAAEPfZfx6Iw790.png"
         *     }
         * ]
         */
        //将数组转换成List<Map>类型

        List<Map> contentList = JSONArray.parseArray(content, Map.class);
        for (Map map : contentList) {
            if (map.get("type").equals("text")) {
                sb.append(map.get("value"));
            } else if (map.get("type").equals("image")) {
                images.add((String) map.get("value"));
            }

        }

        //将标题也加入审核的内容范围中
        if (StringUtils.isNotEmpty(wmNews.getTitle())) {
            //水很冰,毒xxx
            sb.append(",");
            sb.append(wmNews.getTitle());
        }


        //type为0代表无图文章
        //把封面的图片，也添加到检测列表里来
        if (wmNews.getImages() != null && wmNews.getType() != 0) {
            //a.jpg,b.jpg
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        //封装一个map进行返回
        Map<String, Object> resultMap = new HashMap<>();
        //文本内容
        resultMap.put("content", sb.toString());
        //图片内容
        //List<String> collect = images.stream().map(item -> item.replace("http://", "")).collect(Collectors.toList());
        resultMap.put("images", new ArrayList<>(images));
        return resultMap;

    }

    @Autowired
    ArticleFeign articleFeign;

    /**
     * 保存app文章相关的数据
     *
     * @param wmNews
     */
    private void saveAppArticle(WmNews wmNews) {
        //保存app文章
        ApArticle apArticle = saveArticle(wmNews);
        //保存app文章配置
        saveArticleConfig(apArticle);
        //保存app文章内容
        saveArticleContent(apArticle, wmNews);

        //修改自媒体文章的状态为9
        wmNews.setArticleId(Math.toIntExact(apArticle.getId()));
        updateWmNews(wmNews, (short) 9, "审核通过");

        //TODO es索引创建

    }

    /**
     * 创建app端文章内容信息
     *
     * @param apArticle
     * @param wmNews
     */
    private void saveArticleContent(ApArticle apArticle, WmNews wmNews) {
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        apArticleContent.setContent(wmNews.getContent());
        articleFeign.saveArticleContent(apArticleContent);
    }

    /**
     * 创建app端文章配置信息
     *
     * @param apArticle
     */
    private void saveArticleConfig(ApArticle apArticle) {
        //1.article微服务中封装1个接口
        //2.封装实体类XXXDto封装了保存文章需要的参数

        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        //可以转发
        apArticleConfig.setIsForward(true);
        //已被删除
        apArticleConfig.setIsDelete(false);
        //已下架
        apArticleConfig.setIsDown(false);
        //可以评论
        apArticleConfig.setIsComment(true);

        articleFeign.saveArticleConfig(apArticleConfig);
    }

    @Autowired
    AdChannelMapper adChannelMapper;

    /**
     * 保存文章
     *
     * @param wmNews
     * @return
     */
    private ApArticle saveArticle(WmNews wmNews) {
        ApArticle apArticle = new ApArticle();
        apArticle.setTitle(wmNews.getTitle());
        //文章布局  0 无图文章  1 单图文章  2 多图文章
        apArticle.setLayout(wmNews.getType());
        apArticle.setImages(wmNews.getImages());
        apArticle.setCreatedTime(new Date());

        //获取作者相关信息
        Integer wmUserId = wmNews.getUserId();
        //通过feign服务查询自媒体人
        WmUser wmUser = wemediaFeign.findWmUserById(Long.valueOf(wmUserId));
        if (wmUser != null) {
            //获取自媒体用户中的名字
            String wmUserName = wmUser.getName();
            //根据自媒体用户名字，通过feign接口获取作者信息
            ApAuthor apAuthor = articleFeign.selectAuthorByName(wmUserName);
            if (apAuthor != null) {
                apArticle.setAuthorId(apAuthor.getId().longValue());
                apArticle.setAuthorName(apAuthor.getName());
            }

        }


        //获取频道相关信息
        Integer channelId = wmNews.getChannelId();
        AdChannel channel = adChannelMapper.selectById(channelId);
        if (channel != null) {
            apArticle.setChannelId(channel.getId());
            apArticle.setChannelName(channel.getName());
        }

        return articleFeign.saveAparticle(apArticle);
    }

}
