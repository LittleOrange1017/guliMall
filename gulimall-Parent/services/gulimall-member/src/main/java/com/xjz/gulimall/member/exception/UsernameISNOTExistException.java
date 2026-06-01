package com.xjz.gulimall.member.exception;

public class UsernameISNOTExistException extends RuntimeException{
    public UsernameISNOTExistException(){
        super("用户名错误！");
    }
}
