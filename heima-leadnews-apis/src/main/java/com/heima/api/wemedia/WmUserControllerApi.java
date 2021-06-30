package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;

/**
 * @program: heima-leadnews
 * @description: 自媒体用户
 * @author: xlg
 * @create: 2021-06-13 21:14
 **/

public interface WmUserControllerApi {
    /**
     * 保存自媒体用户
     * @param wmUser
     * @return
     */
    public ResponseResult save(WmUser wmUser);
    /**
     * 按照名称查询用户
     * @param name
     * @return
     */
    public WmUser findByName(String name);
    /**
     * 根据id查询自媒体用户
     * @param id
     * @return
     */
    WmUser findWmUserById(Long id);

}
