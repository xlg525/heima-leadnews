package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmUserDto;

public interface LoginControllerApi {
    /**
     * 自媒体登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmUserDto dto);
}
