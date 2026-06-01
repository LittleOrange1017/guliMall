package com.xjz.gulimall.auth.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginDto {
    private String loginacct;//支持多通道，可以是用户名，也可以是手机号，也可以是邮箱
    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 20,message = "密码长度必须在 6-20 位之间")
    private String password;
}
