package com.xjz.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GulimallProductApplication
 * Package:com.xjz.gulimall.product
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/11 13:39
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class,args);
    }
}
