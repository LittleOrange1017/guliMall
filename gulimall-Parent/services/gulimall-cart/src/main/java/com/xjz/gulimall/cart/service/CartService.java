package com.xjz.gulimall.cart.service;

import com.xjz.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

public interface CartService {

    CartItem addToCart(String skuId, Integer skuNum) throws ExecutionException, InterruptedException;

    CartItem getCartItem(String skuId);
}
