package com.xjz.gulimall.thirdparty.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("sendmsg")
public class SmsMsgProperties {
    private String host;
    private String path;
    private String appcode;
    private String templateId;
    private String smsSignId;
}
