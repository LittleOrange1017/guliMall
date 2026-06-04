package com.xjz.gulimall.cart.constant;

public class CartConstant {
    /**
     * 临时用户 Cookie 的名称
     */
    public static final String TEMP_USER_COOKIE_NAME = "user-key";

    /**
     * 临时用户 Cookie 的过期时间：1 个月（单位：秒）
     */
    public static final int TEMP_USER_COOKIE_TIMEOUT =  30* 24 * 60 * 60;

    /**
     * 购物车 Redis 前缀
     */
    public static final String CART_PREFIX = "gulimall:cart:";
}
