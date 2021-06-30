package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

/**
 * 自媒体文章接口
 */
public interface WmNewsControllerApi {

    /**
     * 分页带条件查询自媒体文章列表
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto wmNewsPageReqDto);
    /**
     * 提交文章
     * @param wmNews
     * @return
     */
    ResponseResult summitNews(WmNewsDto wmNews);
    /**
     * 根据id获取文章信息
     * @return
     */
    ResponseResult findWmNewsById(Integer id);
    /**
     * 删除文章
     * @return
     */
    ResponseResult delNews(Integer id);
    /**
     * 上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);
    /**
     * 根据id查询文章
     * @param id
     * @return
     */
    WmNews findById(Integer id);
    /**
     * 修改文章
     * @param wmNews
     * @return
     */
    ResponseResult updateWmNews(WmNews wmNews);
}