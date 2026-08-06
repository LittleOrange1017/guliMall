package com.xjz.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("alipay")
public class AlipayConfigProperties {
    private String appId;
    private String merchantprivatekey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl;
    private String charset;
    private String signType;
}
