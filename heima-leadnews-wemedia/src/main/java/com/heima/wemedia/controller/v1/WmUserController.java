package com.heima.wemedia.controller.v1;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.api.wemedia.WmUserControllerApi;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: heima-leadnews
 * @description: controller
 * @author: xlg
 * @create: 2021-06-13 21:25
 **/
@RestController
@RequestMapping("/api/v1/user")
public class WmUserController implements WmUserControllerApi {

    @Autowired
    private WmUserService userService;

    @Override
    @PostMapping("/save")
    public ResponseResult save(@RequestBody  WmUser wmUser) {
        userService.save(wmUser);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @GetMapping("/findByName/{name}")
    public WmUser findByName(@PathVariable("name") String name) {
        List<WmUser> list = userService.list(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, name));
        if (list!=null && !list.isEmpty()){
            return list.get(0);
        }
        return null;

    }
    @GetMapping("findOne/{id}")
    @Override
    public WmUser findWmUserById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }
}
