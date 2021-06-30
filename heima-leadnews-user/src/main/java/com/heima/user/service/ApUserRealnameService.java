package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUserRealname;

/**
 * @program: heima-leadnews
 * @description: service
 * @author: xlg
 * @create: 2021-06-12 21:17
 **/

public interface ApUserRealnameService extends IService<ApUserRealname> {
    /**
     * 按照状态分页查询用户列表
     * @param dto
     * @return
     */
    public ResponseResult loadListByStatus(AuthDto dto);
    /**
     * 修改认证用户状态
     * @param dto
     * @param status
     * @return
     */
    public ResponseResult updateStatusById(AuthDto dto,Short status);

}
