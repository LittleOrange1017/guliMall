package com.xjz.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xjz.gulimall.cart.feign.ProductFeign;
import com.xjz.gulimall.cart.interceptor.CartInterceptor;
import com.xjz.gulimall.cart.service.CartService;
import com.xjz.gulimall.cart.vo.CartItem;
import com.xjz.gulimall.cart.vo.SkuInfoVo;
import com.xjz.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import utils.R;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    private final String CART_PREFIX="gulimall:cart:";
    @Override
    public CartItem addToCart(String skuId, Integer skuNum) throws ExecutionException, InterruptedException {
        //获取了当前key所绑定的购物车对象
        //这一个key包含了多个key：value的对象；key代表的就是每一个选中的商品对象，value代表的就是这个cartItem的基本信息
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //判断Redis购物车中是否已经有该商品
        String res = (String) cartOps.get(skuId);
        if(StringUtils.isEmpty(res))
        {
            //购物车中没有此商品，就新增value
            CartItem cartItem=new CartItem();
            //派任务1去查询 sku基本信息
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    R info = productFeign.info(Long.valueOf(skuId));
                    SkuInfoVo skuInfo = new ObjectMapper().convertValue(info.get("skuInfo"), SkuInfoVo.class);
                    cartItem.setCount(skuNum);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setPrice(skuInfo.getPrice());
                    cartItem.setSkuId(skuInfo.getSkuId());
                }
            }, threadPoolExecutor);
            //派任务2去查询sku销售属性
            CompletableFuture<Void> task2 = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    R skuSaleAttrValues = productFeign.getSkuSaleAttrValues(Long.valueOf(skuId));
                    List<String> skuAttr = new ObjectMapper().convertValue(skuSaleAttrValues.get("skuAttr"), new TypeReference<List<String>>() {
                    });
                    cartItem.setSkuAttr(skuAttr);
                }
            }, threadPoolExecutor);
            CompletableFuture.allOf(task1, task2).get();
            // 将组装好的 CartItem 序列化为 JSON 存入 Redis Hash
            cartOps.put(skuId, JSON.toJSONString(cartItem));
            return cartItem;
        }
        else
        {
            //购物车中已有该商品，那么只需更改商品数量即可。
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount()+skuNum);
            cartOps.put(skuId,JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(String skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId);
        if (!StringUtils.isEmpty(str)) {
            return JSON.parseObject(str, CartItem.class);
        }
        return null;
    }

    /**
     * 获取当前用户购物车在Redis中的BoundHashOperations操作对象
     * <p>
     * 根据用户身份（登录用户/临时用户）构建不同的Redis Key，
     * 登录用户使用userId作为标识，临时用户使用userKey（UUID）作为标识。
     * </p>
     *
     * @return 绑定到对应购物车Key的Hash操作对象，用于对购物车进行增删改查
     */
    private BoundHashOperations<String, Object, Object> getCartOps(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String key="";
        // 根据用户类型选择不同的Key拼接策略
        if(!(userInfoTo.getUserId() ==null))
        {
            key=CART_PREFIX+userInfoTo.getUserId().toString();
        }
        else
        {
            key=CART_PREFIX+userInfoTo.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(key);
    }

}
