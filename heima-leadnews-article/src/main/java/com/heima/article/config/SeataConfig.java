package com.heima.article.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @program: heima-leadnews
 * @description: seata
 * @author: xlg
 * @create: 2021-06-15 20:52
 **/
@Configuration
@ComponentScan("com.heima.seata.config")
public class SeataConfig {
}
