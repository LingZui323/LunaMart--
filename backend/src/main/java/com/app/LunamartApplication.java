package com.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 月光商城项目启动类
 * @author LunaMart
 */
@SpringBootApplication
@MapperScan("com.app.mart.mapper")
public class LunamartApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunamartApplication.class, args);
    }

}