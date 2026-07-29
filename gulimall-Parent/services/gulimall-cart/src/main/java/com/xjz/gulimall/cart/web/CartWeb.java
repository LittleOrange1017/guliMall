package com.xjz.gulimall.cart.web;

import com.xjz.gulimall.cart.interceptor.CartInterceptor;
import com.xjz.gulimall.cart.service.CartService;
import com.xjz.gulimall.cart.vo.CartItem;
import com.xjz.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class CartWeb {
    @Autowired
    private CartService cartService;
    @GetMapping("/")
    public String cartPage(Model model){
        return "cartlist";
    }
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") String skuId, @RequestParam("skuNum") Integer skuNum, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, skuNum);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.littleorange.com/addToCartSuccess.html";
    }
    /**
     * 加购成功展示页面（GET 请求，随意刷新不会重复加购）
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") String skuId, Model model) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        // 从 Redis 查询出刚刚加入的购物项数据
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        model.addAttribute("userInfo",userInfoTo);
        return "success"; // 渲染 success.html 页面
    }
}
