package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdChannelMapper;
import com.heima.admin.service.AdChannelService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AdChannelServiceImpl extends ServiceImpl<AdChannelMapper, AdChannel> implements AdChannelService {
    @Override
    public ResponseResult findByNameAndPage(ChannelDto dto) {
        //1.参数检测
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数检查
        dto.checkParam();
        //2.安装名称模糊分页查询
        Page page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdChannel> lambdaQueryWrapper = new LambdaQueryWrapper();
        if (StringUtils.isNotBlank(dto.getName())) {
            //数据字段名修改只需要修改属性上面的注解，代码不需要修改，lambdaQueryWrapper可以起到解耦合作用
            lambdaQueryWrapper.like(AdChannel::getName, dto.getName());
            //QueryWrapper<AdChannel> QueryWrapper = new QueryWrapper<>();
        }
        IPage result = page(page, lambdaQueryWrapper);
        //Page page1 = new Page(dto.getPage(), dto.getSize());
        //3.结果封装
        ResponseResult responseResult = new PageResponseResult(/*dto.getPage()*/ (int) result.getCurrent(), dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
        //isnotblank和isempty区别  isNotBlank会把空格看成false 而isempty会把空格看成true
        /*public static boolean isEmpty(final CharSequence cs) {
            return cs == null || cs.length() == 0;
        }
        *//**
         * <p>Checks if a CharSequence is not empty ("") and not null.</p>
         *
         * <pre>
         * StringUtils.isNotEmpty(null)      = false
         * StringUtils.isNotEmpty("")        = false
         * StringUtils.isNotEmpty(" ")       = true
         * StringUtils.isNotEmpty("bob")     = true
         * StringUtils.isNotEmpty("  bob  ") = true
         * </pre>
         *
         * @param cs  the CharSequence to check, may be null
         * @return {@code true} if the CharSequence is not empty and not null
         * @since 3.0 Changed signature from isNotEmpty(String) to isNotEmpty(CharSequence)
         */
    }
    @Override
    public ResponseResult insert(AdChannel adchannel) {
        //1.检查参数
        if (null == adchannel) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.保存数据
        adchannel.setCreatedTime(new Date());
        save(adchannel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult update(AdChannel channel) {
        //1.检查参数
        if (null == channel) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        updateById(channel);
        return  ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult deleteById(Integer id) {
        //1.检查参数
        if (null == id) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.判断当前频道是否存在 和 是否有效
        AdChannel channel = getById(id);
        if (channel==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (channel.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //3.删除频道
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);


    }
}
