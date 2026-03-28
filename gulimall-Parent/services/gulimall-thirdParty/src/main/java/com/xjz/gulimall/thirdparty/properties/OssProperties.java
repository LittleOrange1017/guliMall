package com.xjz.gulimall.thirdparty.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: OssProperties
 * Package:com.xjz.gulimall.thirdparty.properties
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/26 18:58
 * @Version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String stsRoleArn;
}
