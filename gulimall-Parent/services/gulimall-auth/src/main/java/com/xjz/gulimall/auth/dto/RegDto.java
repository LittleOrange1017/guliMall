package com.xjz.gulimall.auth.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class RegDto {
    @NotEmpty(message = "用户名必须填写")
    @Pattern(regexp = "^.*\\D.*$", message = "用户名不能为纯数字")
    @Length(min = 4,max = 20,message = "用户名长度必须在 4-20 位之间")
    private String userName;
    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 20,message = "密码长度必须在 6-20 位之间")
    private String password;
    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^(138|189|132|139|158|188|157|181|152|137|183)\\d{8}$", message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;
}
