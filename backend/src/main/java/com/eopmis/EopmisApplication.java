package com.eopmis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * EOPMIS 后端启动类
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.eopmis.mapper")
public class EopmisApplication {
    public static void main(String[] args) {
        SpringApplication.run(EopmisApplication.class, args);
    }
}
