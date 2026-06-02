package com.xjz.gulimall.member.exception;

public class SocialUserISExistException extends RuntimeException{
    public SocialUserISExistException(){
        super("社交用户已经存在，请直接登录");
    }
}
