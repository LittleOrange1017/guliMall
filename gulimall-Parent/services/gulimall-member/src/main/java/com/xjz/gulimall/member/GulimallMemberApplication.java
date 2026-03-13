package com.xjz.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GulimallMemberApplication
 * Package:com.xjz.gulimall.member
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/11 13:38
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class,args);
    }
}
