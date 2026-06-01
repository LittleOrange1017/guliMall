package com.xjz.gulimall.auth.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class RegFeignDto {
    private String userName;
    private String password;
    private String phone;
}
