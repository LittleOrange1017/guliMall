package com.xjz.gulimall.cart.vo;

import lombok.Data;

@Data
public class UserInfoTo {
    private Long userId;        // 登录用户的 ID（若已登录则不为 null）
    private String userKey;     // 游客/临时用户的 UUID 标识
    private boolean tempUser = false; // 标记 user-key 是否为本次请求新生成的临时标识
    private String img;
    private String userName;
}
