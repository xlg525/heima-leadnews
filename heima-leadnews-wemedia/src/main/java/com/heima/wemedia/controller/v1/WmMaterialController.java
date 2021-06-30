package com.heima.wemedia.controller.v1;

import com.heima.api.wemedia.WmMaterialControllerApi;
import com.heima.common.constans.wemedia.WemediaContans;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController implements WmMaterialControllerApi {
    @Autowired
    private WmMaterialService materialService;

    @PostMapping("/upload_picture")
    @Override
    public ResponseResult uploadPicture(MultipartFile file) {
        return materialService.uploadPicture(file);
    }
    @RequestMapping("/list")
    @Override
    public ResponseResult findList(@RequestBody WmMaterialDto dto) {
        return materialService.findList(dto);
    }
    @GetMapping("/del_picture/{id}")
    @Override
    public ResponseResult delPicture(@PathVariable("id") Integer id) {
        return materialService.delPicture(id);
    }
    @GetMapping("/cancel_collect/{id}")
    @Override
    public ResponseResult cancleCollectionMaterial(Integer id) {
        return materialService.updateStatus(id, WemediaContans.CANCEL_COLLECT_MATERIAL);
    }
    @GetMapping("/collect/{id}")
    @Override
    public ResponseResult collectionMaterial(Integer id) {
        return materialService.updateStatus(id, WemediaContans.COLLECT_MATERIAL);
    }


}