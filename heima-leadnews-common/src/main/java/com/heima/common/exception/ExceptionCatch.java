package com.heima.common.exception;

/**
 * @program: heima-leadnews
 * @description: 异常处理
 * @author: xlg
 * @create: 2021-06-09 21:06
 **/

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice//控制器增强
@Log4j2
public class ExceptionCatch {
    //捕获Exception类异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exection(Exception exception) {
        exception.printStackTrace();
        //记录日志
        log.error("catch exception:{}", exception.getMessage());
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }
}
