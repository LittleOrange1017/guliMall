package com.xjz.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GulimallGatewayApplication
 * Package:com.xjz.gulimall.gateway
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/14 10:34
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallGatewayApplication.class,args);
    }
}
