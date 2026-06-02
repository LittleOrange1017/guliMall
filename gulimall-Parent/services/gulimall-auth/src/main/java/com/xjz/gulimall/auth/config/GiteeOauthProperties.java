package com.xjz.gulimall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth.gitee")
@Data
public class GiteeOauthProperties {
    private String client_Id;
    private String Client_Secret;
    private String redirect_uri;
}
