package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class AuthDto extends PageRequestDto {

    /**
     * 状态
     */
    private Short status;

    /**
     * 认证的id
     */
    private Integer id;

    /**
     * 拒绝原因
     */
    private String msg;

}