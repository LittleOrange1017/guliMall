package com.xjz.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginFeginVo {
    private String loginacct;//支持多通道，可以是用户名，也可以是手机号，也可以是邮箱
    private String password;
}
