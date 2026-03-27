package com.xjz.gulimall.product.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: OssProperties
 * Package:com.xjz.gulimall.product.properties
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/26 18:58
 * @Version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.oss")
public class OssProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
}
