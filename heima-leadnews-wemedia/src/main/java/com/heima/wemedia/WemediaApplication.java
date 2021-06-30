package com.heima.wemedia;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * @program: heima-leadnews
 * @description: 启动类
 * @author: xlg
 * @create: 2021-06-13 21:37
 **/
@SpringBootApplication(scanBasePackages = "com.heima.wemedia")
@EnableDiscoveryClient
@ServletComponentScan
@MapperScan("com.heima.wemedia.mapper")
public class WemediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }
    /*
    * mybatis-plus分页插件*/
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
