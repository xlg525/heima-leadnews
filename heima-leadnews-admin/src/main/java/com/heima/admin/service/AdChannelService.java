package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.data.annotation.Id;


public interface AdChannelService extends IService<AdChannel> {
    /**
     * 根据名称分页查询频道列表
     * @param dto
     * @return
     */
    public ResponseResult findByNameAndPage(ChannelDto dto);
    /**
     * 新增
     * @param channel
     * @return
     */
    public ResponseResult insert(AdChannel channel);
    /**
     * 修改
     * @param channel
     * @return
     */
    public ResponseResult update(AdChannel channel);
    /**
     * 删除
     * @param id
     * @return
     */
    public ResponseResult deleteById(Integer id);
}
