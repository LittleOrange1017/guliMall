package com.xjz.gulimall.order.interceptor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo)session.getAttribute("loginUser");
        if(memberVo!=null&&memberVo.getUserId()!=null)
        {
            loginUser.set(memberVo);
            return true;
        }
        response.sendRedirect("http://auth.littleorange.com/login");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        loginUser.remove();
    }
}
