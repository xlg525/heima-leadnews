package com.heima.wemedia.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @program: heima-leadnews
 * @description: 启动类
 * @author: xlg
 * @create: 2021-06-21 19:10
 **/
@SpringBootApplication
@EnableDiscoveryClient//注册nacos中
public class WemediaGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WemediaGateWayApplication.class,args);
    }
}
