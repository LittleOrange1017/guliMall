package com.xjz.gulimall.cart.service;

import com.xjz.gulimall.cart.vo.Cart;
import com.xjz.gulimall.cart.vo.CartItem;
import com.xjz.gulimall.cart.vo.UserInfoTo;

import java.util.concurrent.ExecutionException;

public interface CartService {

    CartItem addToCart(String skuId, Integer skuNum) throws ExecutionException, InterruptedException;

    CartItem getCartItem(String skuId);

    Cart listCart(UserInfoTo userInfoTo);

    void checkCartItem(String skuId, Integer isChecked);

    void changeItemCount(String skuId, Integer num);
}
