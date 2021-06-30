package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constans.wemedia.WemediaContans;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.threadlocal.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsService;
import com.sun.org.apache.xpath.internal.objects.XNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Value("${fdfs.url}")
    private String fileServerUrl;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        //1.参数检查
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //分页参数检查
        dto.checkParam();
        //2.分页条件查询
        IPage pageParam = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //状态精确查询 where status = #{status}
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        //频道精确查询 where channel_id = channelId
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        //关键字模糊查询
        if (StringUtils.isNotEmpty(dto.getKeyWord())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyWord());
        }

        //时间范围查询 where publish_time between #{begin} and #{endTime}
        //1. where publish_time &gt;= #{begin} and publish_time &lt;= #{endTime}
        //2. <![CDATA[ where publish_time >= #{begin} and publish_time <= #{endTime}     ]]>
        if (dto.getBeginPubdate() != null && dto.getEndPubdate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubdate(), dto.getEndPubdate());
        }

        //根据自媒人精确查询 我只能看到我自己的文章
        lambdaQueryWrapper.eq(WmNews::getUserId, user.getId());

        //按照发布时间倒序查询
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);
        IPage pageResult = page(pageParam, lambdaQueryWrapper);

        //3.结果封装返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageResult.getTotal());
        responseResult.setData(pageResult.getRecords());
        //设置返回结果中host -> http://192.168.226.134:8888
        responseResult.setHost(fileServerUrl);
        return responseResult;
    }


    @Override
    public ResponseResult saveNews(WmNewsDto dto, Short isSubmit) {
        //1.检查参数
        if (dto == null || StringUtils.isBlank(dto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.保存或修改文章
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        if (WemediaContans.WM_NEWS_AUTO_TYPE.equals(dto.getType())) {
            wmNews.setType(null);
        }
        if (dto.getImages() != null && dto.getImages().size() > 0) {
            //[dfjksdjfdfj.jpg,sdlkjfskld.jpg]
            wmNews.setImages(dto.getImages().toString().replace("[", "")
                    .replace("]", "").replace(fileServerUrl, "")
                    .replace(" ", ""));
        }
        //保存或修改文章
        saveWmNews(wmNews, isSubmit);


        //3.关联文章与素材的关系
        String content = dto.getContent();
        List<Map> list = JSON.parseArray(content, Map.class);
        List<String> materials = ectractUrlInfo(list);

        //3.1 关联内容中的图片与素材的关系
        if (isSubmit == WmNews.Status.SUBMIT.getCode() && materials.size() != 0) {
            ResponseResult responseResult = saveRelativeInfoForContent(materials, wmNews.getId());
            if (responseResult != null) {
                return responseResult;
            }
        }

        //3.2 关联封面中的图片与素材的关系,设置wm_news的type,自动
        if (isSubmit == WmNews.Status.SUBMIT.getCode()) {
            ResponseResult responseResult = saveRelativeInfoForCover(dto, materials, wmNews);
            if (responseResult != null) {
                return responseResult;
            }
        }


        return null;
    }

    /**
     * 设置封面图片与素材的关系
     *
     * @param dto
     * @param materials
     * @param wmNews
     * @return
     */
    private ResponseResult saveRelativeInfoForCover(WmNewsDto dto, List<String> materials, WmNews wmNews) {
        List<String> images = dto.getImages();
        //保存封面图片与素材的关系
        if (images != null && images.size() > 0) {
            ResponseResult responseResult = saveRelativeInfoForImage(images, wmNews.getId());
            if (responseResult != null) {
                return responseResult;
            }
        }
        return null;
    }

    /**
     * @param images
     * @param newsId
     * @return
     */
    private ResponseResult saveRelativeInfoForImage(List<String> images, Integer newsId) {
        List<String> materials = new ArrayList<>();
        for (String image : images) {
            materials.add(image.replace(fileServerUrl,""));
        }

        return saveRelativeInfo(materials,newsId,WemediaContans.WM_NEWS_COVER_REFERENCE);
    }

    /**
     * 保存素材与文章内容的关系
     *
     * @param materials
     * @param newsId
     * @return
     */
    private ResponseResult saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        //引用类型：0为内容引用 1为主图引用
        return saveRelativeInfo(materials, newsId, WemediaContans.WM_NEWS_CONTENT_REFERENCE);
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    /**
     * 保存关系
     *
     * @param materials
     * @param newsId
     * @param type
     * @return
     */
    private ResponseResult saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        //1.获取数据库中的素材信息
        //参数1为素材URL，但是保存到数据库需要素材的ID，所以需要从数据库查询一次
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //素材ID属于范围
        //select * from wm_material where url in ('a','b','c') and user_id = 1000
        lambdaQueryWrapper.in(WmMaterial::getUrl, materials);
        //素材用户ID等于操作用户ID
        lambdaQueryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtils.getUser().getId());
        List<WmMaterial> dbMaterials = wmMaterialMapper.selectList(lambdaQueryWrapper);

        List<Integer> collect = dbMaterials.stream().map(item -> item.getId()).collect(Collectors.toList());

        if(materials.size()  !=  collect.size()){
            //有图片不存在数据库中
            //return ResponseResult.errorResult(Ap)
        }

        if(dbMaterials.size() > 0){
            List<String> mIds = new ArrayList<>();
            //取到素材ID,没有必要
            for (WmMaterial dbMaterial : dbMaterials) {
                mIds.add(String.valueOf(dbMaterial.getId()));
            }

            //批量添加到关联表中 10000 500
            wmNewsMaterialMapper.saveRelations(mIds,newsId,type);

            //1.使用mybatis-plus批量添加方法
//            List<WmNewsMaterial> datas = new ArrayList<>();
//            short count = 1;
//            for (WmMaterial dbMaterial : dbMaterials) {
//                WmNewsMaterial wmNewsMaterial = new WmNewsMaterial();
//                wmNewsMaterial.setMaterialId(dbMaterial.getId());
//                wmNewsMaterial.setNewsId(newsId);
//                wmNewsMaterial.setType(type);
//                wmNewsMaterial.setOrd(count++);
//                datas.add(wmNewsMaterial);
//            }
//            saveBatch(datas);
        }


        return null;
    }

    /**
     * 提取图片信息
     *
     * @param list
     * @return
     */
    private List<String> ectractUrlInfo(List<Map> list) {
        List<String> materials = new ArrayList<>();
        for (Map map : list) {
            //判断是否类型为image，只有为image类型的才是图片
            if (map.get("type").equals(WemediaContans.WM_NEWS_TYPE_IMAGE)) {
                //获取value值，就是图片的地址
                String imgUrl = (String) map.get("value");
                //删除图片路径中服务器相关的信息
                //"value":"http://192.168.200.130/group1/M00/00/00/wKjIgl5swbGATaSAAAEPfZfx6Iw790.png"
                imgUrl = imgUrl.replace(fileServerUrl, "");
                //"/group1/M00/00/00/wKjIgl5swbGATaSAAAEPfZfx6Iw790.png"
                materials.add(imgUrl);
            }
        }
        return materials;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存或修改文章
     *
     * @param wmNews
     * @param isSubmit
     */
    private void saveWmNews(WmNews wmNews, Short isSubmit) {
        //0草稿 1待审核
        wmNews.setStatus(isSubmit);
        //设置userId为当前登录用户ID
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());
        //设置创建时间
        wmNews.setCreatedTime(new Date());
        //设置提交时间
        wmNews.setSubmitedTime(new Date());
        //1代表上架
        wmNews.setEnable((short) 1);
        //处理添加或者修改
        if (wmNews.getId() == null) {
            //mybatis-plus如果处理自增ID，在写入到数据库之后，将ID回写到实体对象
            save(wmNews);
        } else {
            //如果是修改，则先删除素材与文章的关系
            LambdaQueryWrapper<WmNewsMaterial> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(WmNewsMaterial::getNewsId, wmNews.getId());
            wmNewsMaterialMapper.delete(queryWrapper);
            updateById(wmNews);
        }
    }
    @Override
    public ResponseResult findWmNewsById(Integer id) {
        //1.参数检查
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章Id不可缺少");
        }
        //2.查询数据
        WmNews wmNews = getById(id);
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }

        //3.结果返回
        ResponseResult responseResult = ResponseResult.okResult(wmNews);
        responseResult.setHost(fileServerUrl);
        return responseResult;
    }
    @Override
    public ResponseResult delNews(Integer id) {
        //1.检查参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章Id不可缺少");
        }
        //2.获取数据
        WmNews wmNews = getById(id);
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }

        //3.判断当前文章的状态  status==9  enable == 1
        if(wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode()) && wmNews.getEnable().equals(WemediaContans.WM_NEWS_ENABLE_UP)){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章已发布，不能删除");
        }

        //4.去除素材与文章的关系
        wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));

        //5.删除文章
        removeById(wmNews.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        //1.检查参数
        if(dto == null || dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.查询文章
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }

        //3.判断文章是否发布
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"当前文章不是发布状态，不能上下架");
        }

        //4.修改文章状态，同步到app端（后期做）TODO
        if(dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2){
            update(Wrappers.<WmNews>lambdaUpdate().eq(WmNews::getId,dto.getId()).set(WmNews::getEnable,dto.getEnable()));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}
