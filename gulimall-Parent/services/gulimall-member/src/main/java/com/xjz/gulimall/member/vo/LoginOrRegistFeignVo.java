package com.xjz.gulimall.member.vo;

import lombok.Data;

@Data
public class LoginOrRegistFeignVo {
    private Long id;           // 🌟 Gitee 用户唯一主键 ID
    private String name;       // 昵称（如：小明）
    private String login;      // 登录名 / 账号（如：xiaoming）
    private String avatar_url; // 头像链接
    private String email;
}
