package com.zjj.aisearch;

import com.zjj.aisearch.config.ConfigBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @program: aisearch
 * @description: 启动类
 * @author: zjj
 * @create: 2019-09-07 16:49:19
 **/

@SpringBootApplication
@MapperScan(basePackages = "com.zjj.aisearch.mapper")
@EnableConfigurationProperties(ConfigBean.class)
//扫描servlet
//@EnableJms //启动消息队列
@ServletComponentScan
public class SpringbootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }
}
