package com.heima.admin.controller.v1;

import com.heima.admin.service.AdChannelService;
import com.heima.api.admin.AdChannelControllerApi;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/channel")
@ApiOperation(value = "根据分页查询频道列表")
public class AdChannelController implements AdChannelControllerApi {
    @Autowired
    private AdChannelService adChannelService;
    @PostMapping("/list")
    @Override
    //@RequestMapping(path = "/list",method = {RequestMethod.POST,RequestMethod.GET})//支持多种请求方式
    public ResponseResult findByNameAndPage(@RequestBody ChannelDto dto) {
        return adChannelService.findByNameAndPage(dto);
    }
    @Override
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannel channel) {
        return adChannelService.insert(channel);
    }

    @Override
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel channel) {
        return adChannelService.update(channel);
    }

    @Override
    @GetMapping("/del/{id}")
    public ResponseResult deleteById(@PathVariable("id") Integer id) {
        return adChannelService.deleteById(id);
    }
}
