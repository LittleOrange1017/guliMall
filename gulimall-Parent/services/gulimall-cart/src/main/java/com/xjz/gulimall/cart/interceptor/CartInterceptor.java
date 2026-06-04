package com.xjz.gulimall.cart.interceptor;

import com.xjz.gulimall.cart.constant.CartConstant;
import com.xjz.gulimall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import vo.MemberVo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

public class CartInterceptor implements HandlerInterceptor {
    // 线程局部变量，用于在同一请求线程（Interceptor -> Controller -> Service）内传递 UserInfoTo
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo=new UserInfoTo();
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo)session.getAttribute("loginUser");
        if(memberVo!=null)
        {
            /**
             * 用户已登录，设置UserId
             */
            userInfoTo.setUserId(memberVo.getUserId());
        }
        /**
         * 用户未登录，检查用户是否第一次来到此网站，换句话说就是检查此次请求有无携带cookie
         */
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    // 标记为已有临时用户，不需要写回新 Cookie
                    userInfoTo.setTempUser(false);
                }
            }
        }
        /**
         * 用户第一次访问或者cookie已过期
         */
        //强制生成一个UUID，并写入cookie
        if(StringUtils.isEmpty(userInfoTo.getUserKey()))
        {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
            // 标记为新生成的临时用户，需要在 postHandle 中写回 Cookie
            userInfoTo.setTempUser(true);
        }
        //将组装好的用户信息存入 ThreadLocal
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        //如果user-key是本次请求新生成的，必须写入 Cookie 返回给浏览器保存 1 个月
        if(userInfoTo!=null&&userInfoTo.isTempUser())
        {
            Cookie cookie=new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("littleorange.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
