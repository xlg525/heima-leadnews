package com.heima.article;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * @program: heima-leadnews
 * @description: 引导类
 * @author: xlg
 * @create: 2021-06-13 22:14
 **/
@SpringBootApplication(scanBasePackages = {"com.heima.article"},exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@MapperScan("com.heima.article.mapper")
public class ArticleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleApplication.class,args);
    }
    /*
     * mybatis-plus分页插件*/
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
