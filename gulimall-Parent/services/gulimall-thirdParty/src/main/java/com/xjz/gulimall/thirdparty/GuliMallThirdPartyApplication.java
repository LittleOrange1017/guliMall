package com.xjz.gulimall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ClassName: GuliMallThirdPartyApplication
 * Package:com.xjz.gulimall.thirdparty
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/27 10:58
 * @Version 1.0
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
@EnableDiscoveryClient
public class GuliMallThirdPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuliMallThirdPartyApplication.class, args);
    }
}
