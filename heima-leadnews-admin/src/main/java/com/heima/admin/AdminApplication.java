package com.heima.admin;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.heima.admin.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
    /*
     * mybatis-plus分页插件
     * */
    @Bean
    public Interceptor paginationInterceptor() {
        return  new PaginationInterceptor();
     /*   MyPaginationInterceptor paginationInterceptor = new MyPaginationInterceptor();
        paginationInterceptor.setOverflow(true);//页数溢出后返回第最后一页
        return paginationInterceptor;
*/
    }


}
