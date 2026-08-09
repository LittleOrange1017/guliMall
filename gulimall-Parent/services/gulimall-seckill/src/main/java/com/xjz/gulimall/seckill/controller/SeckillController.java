package com.xjz.gulimall.seckill.controller;

import com.xjz.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import to.SeckillSkuRedisTo;
import utils.R;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    /**
     * 场景 1：获取当前时间段正在参与秒杀的所有商品列表
     */
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        return seckillService.getCurrentSeckillSkus();
    }

    /**
     * 场景 2：根据 skuId 获取商品的秒杀详情（用于详情页切面）
     */
    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public SeckillSkuRedisTo getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
        return seckillService.getSkuSeckillInfo(skuId);
    }

    /**
     * 秒杀下单接口
     */
    @PostMapping("/kill")
    @ResponseBody
    public R kill(@RequestParam("killId") String killId,
                  @RequestParam("key") String key,
                  @RequestParam("num") Integer num) {
        return seckillService.kill(killId, key, num);
    }
}
