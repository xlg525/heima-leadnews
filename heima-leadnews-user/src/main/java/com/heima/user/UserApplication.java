package com.heima.user;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
/**
 * @program: heima-leadnews
 * @description: 用户认证
 * @author: xlg
 * @create: 2021-06-12 21:07
 **/
@SpringBootApplication(scanBasePackages = {"com.heima.user"},exclude = {DataSourceAutoConfiguration.class})
@MapperScan("com.heima.user.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
    /**
     * mybatis-plus分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
