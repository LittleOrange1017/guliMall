package com.xjz.gulimall.seckill.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberVo> loginUser=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        MemberVo memberVo = session == null ? null : (MemberVo) session.getAttribute("loginUser");
        if(memberVo!=null&&memberVo.getUserId()!=null)
        {
            loginUser.set(memberVo);
            return true;
        }
        return false;
    }
}
