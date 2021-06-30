package com.heima.user.controller;

import com.heima.api.user.ApUserRealnameControllerApi;
import com.heima.common.constans.user.AdminConstans;
import com.heima.common.constans.user.UserConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserRealnameController implements ApUserRealnameControllerApi {

    @Autowired
    private ApUserRealnameService apUserRealnameService;

    @PostMapping("/list")
    @Override
    public ResponseResult loadListByStatus(@RequestBody AuthDto dto) {
        return apUserRealnameService.loadListByStatus(dto);
    }

    @PostMapping("/authPass")
    @Override
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return apUserRealnameService.updateStatusById(dto, UserConstants.PASS_AUTH);
    }

    @PostMapping("/authFail")
    @Override
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return apUserRealnameService.updateStatusById(dto, AdminConstans.FAIL_AUTH);
    }
}