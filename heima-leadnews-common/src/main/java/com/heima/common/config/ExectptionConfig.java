package com.heima.common.config;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: heima-leadnews
 * @description: 自动配置
 * @author: xlg
 * @create: 2021-06-10 19:30
 **/
@ControllerAdvice//控制器增强
@Slf4j
public class ExectptionConfig {
    //捕获Execption此类异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult execption(Exception exception){
        //记录日志
        log.error("catch execption:()",exception);
        //返回通用异常
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }
}
