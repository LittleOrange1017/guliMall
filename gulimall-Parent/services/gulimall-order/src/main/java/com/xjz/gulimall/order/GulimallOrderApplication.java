package com.xjz.gulimall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GulimallOrderApplication
 * Package:com.xjz.gulimall.order
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/11 13:38
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class,args);
    }
}
