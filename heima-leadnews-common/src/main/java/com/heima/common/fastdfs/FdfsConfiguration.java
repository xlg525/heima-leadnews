package com.heima.common.fastdfs;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * @program: fastdfs-demo
 * @description: config
 * @author: xlg
 * @create: 2021-06-20 16:50
 **/
@Configuration
@PropertySource("classpath:fast_dfs.properties")
@Import(FdfsClientConfig.class) // 导入FastDFS-Client组件
public class FdfsConfiguration {
}
