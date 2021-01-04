package com.baidu.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.baidu.shop.mapper")
public class RunShopServicexxx {
    public static void main(String[] args) {
        SpringApplication.run(RunShopServicexxx.class);
    }
}
