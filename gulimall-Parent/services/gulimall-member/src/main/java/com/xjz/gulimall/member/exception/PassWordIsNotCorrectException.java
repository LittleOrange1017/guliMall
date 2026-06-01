package com.xjz.gulimall.member.exception;

public class PassWordIsNotCorrectException extends RuntimeException{
    public PassWordIsNotCorrectException(){
        super("密码错误!");
    }

}
