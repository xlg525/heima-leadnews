package com.heima.admin.controller.v1;

import com.heima.admin.service.AdSensitiveService;
import com.heima.api.admin.AdSensitiveControllerApi;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
public class AdSensitiveController implements AdSensitiveControllerApi {

    @Autowired
    private AdSensitiveService adSensitiveService;

    @GetMapping("/testError")
    public void testError(){int i=1/0;}

    @PostMapping("/list")
    @Override
    public ResponseResult list(@RequestBody SensitiveDto dto) {
        return adSensitiveService.list(dto);
    }

    @PostMapping("/save")
    @Override
    public ResponseResult save(@RequestBody AdSensitive adSensitive) {
        return adSensitiveService.insert(adSensitive);
    }

    @PostMapping("/update")
    @Override
    public ResponseResult update(@RequestBody AdSensitive adSensitive) {
        return adSensitiveService.update(adSensitive);
    }

    @DeleteMapping("/del/{id}")
    @Override
    public ResponseResult deleteById(@PathVariable("id") Integer id) {
        return adSensitiveService.deleteById(id);
    }
}