package com.xjz.gulimall.auth.vo;

import lombok.Data;

@Data
public class GiteeTokenVo {
    private String grant_type;
    private String code;
    private String client_id;
    private String redirect_uri;
    private String client_secret;
}
