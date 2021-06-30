package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.fastdfs.FastDFSClientUtil;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.threadlocal.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    FastDFSClientUtil fastDFSClient;

    @Value("${fdfs.url}")
    private String fileServerUrl;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.检查参数
        if(multipartFile == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到fasfdfs
        String fileId = null;
        try {
            fileId = fastDFSClient.uploadFile(multipartFile);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        //3.保存素材数据到表中 wm_material

        WmUser user = WmThreadLocalUtils.getUser();

        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short)0);
        wmMaterial.setUserId(user.getId());
        wmMaterial.setType((short)0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        //拼接图片路径
        wmMaterial.setUrl(fileServerUrl+fileId);
        return ResponseResult.okResult(wmMaterial);
    }
    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        //1.检查参数
        dto.checkParam();

        //2.带条件分页查询
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //获取当前登录的用户
        Integer uid = WmThreadLocalUtils.getUser().getId();
        lambdaQueryWrapper.eq(WmMaterial::getUserId,uid);
        //是否收藏
        if(dto.getIsCollected() != null && dto.getIsCollected().shortValue()==1){
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection,dto.getIsCollected());
        }
        //按照日期倒序排序
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        IPage pageParam = new Page(dto.getPage(),dto.getSize());
        IPage resultPage = page(pageParam, lambdaQueryWrapper);
        //3.结果返回
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)resultPage.getTotal());
        List<WmMaterial> datas = resultPage.getRecords();
        //为每个图片加上前缀
        datas = datas.stream().map(item->{
            item.setUrl(fileServerUrl+item.getUrl());
            return item;
        }).collect(Collectors.toList());
        responseResult.setData(datas);

        return responseResult;
    }
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Override
    public ResponseResult delPicture(Integer id) {
        //1.检查参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.判断当前图片是否问引用
        WmMaterial wmMaterial = getById(id);
        if(wmMaterial == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        LambdaQueryWrapper<WmNewsMaterial> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(WmNewsMaterial::getMaterialId,id);
        Integer count = wmNewsMaterialMapper.selectCount(lambdaQueryWrapper);
        if(count > 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前图片被引用");
        }
        //3.删除fastdfs中的图片
        String fileId = wmMaterial.getUrl().replace(fileServerUrl, "");
        try {
            fastDFSClient.delFile(fileId);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }

        //4.删除数据库中的图片
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    @Override
    public ResponseResult updateStatus(Integer id, Short type) {
        //1.检查参数
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.更新状态
        //获取当前用户信息
        Integer uid = WmThreadLocalUtils.getUser().getId();
        update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection,type)
                .eq(WmMaterial::getId,id).eq(WmMaterial::getUserId,uid));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}