package com.xjz.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GulimallCouponApplication
 * Package:com.xjz.gulimall.coupon
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/11 13:37
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class,args);
    }
}
