package com.xjz.gulimall.member.exception;

import exception.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import utils.R;

@Slf4j
@RestControllerAdvice
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = PhoneExistException.class)
    public R handlePhoneExistException(PhoneExistException e)
    {
        return R.error().put(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), e.getMessage());
    }
    @ExceptionHandler(value = UsernameExistException.class)
    public R handleUsernameExistException(UsernameExistException e)
    {
        return R.error().put(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), e.getMessage());
    }
}
