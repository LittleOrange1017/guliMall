package com.xjz.gulimall.seckill.service;

import to.Laste3DaysSessionTo;
import to.SeckillSkuRedisTo;
import to.SeckillSkuRelationTo;
import utils.R;

import java.util.List;

public interface SeckillService {

    List<Laste3DaysSessionTo> query3DaysSession();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    R kill(String killId, String key, Integer num);
}
