package com.heima.wemedia.controller.v1;

import com.heima.api.wemedia.WmNewsControllerApi;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController implements WmNewsControllerApi {

    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    @Override
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto wmNewsPageReqDto){
        return wmNewsService.findAll(wmNewsPageReqDto);
    }
    @PostMapping("/submit")
    @Override
    public ResponseResult summitNews(@RequestBody WmNewsDto wmNews) {
        if(wmNews.getStatus()== WmNews.Status.SUBMIT.getCode()){
            //提交文章
            return wmNewsService.saveNews(wmNews, WmNews.Status.SUBMIT.getCode());
        }else{
            //保存草稿
            return wmNewsService.saveNews(wmNews, WmNews.Status.NORMAL.getCode());
        }
    }
    @GetMapping("/one/{id}")
    @Override
    public ResponseResult findWmNewsById(@PathVariable("id") Integer id) {
        return wmNewsService.findWmNewsById(id);
    }
    @GetMapping("/del_news/{id}")
    @Override
    public ResponseResult delNews(@PathVariable("id") Integer id) {
        return wmNewsService.delNews(id);
    }
    @PostMapping("/down_or_up")
    @Override
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto) {
        return wmNewsService.downOrUp(dto);
    }
    @GetMapping("/findOne/{id}")
    @Override
    public WmNews findById(@PathVariable("id") Integer id) {
        return wmNewsService.getById(id);
    }
    @PostMapping("/update")
    @Override
    public ResponseResult updateWmNews(@RequestBody WmNews wmNews) {
        boolean b = wmNewsService.updateById(wmNews);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}