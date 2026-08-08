package com.xjz.gulimall.seckill.service;

import to.Laste3DaysSessionTo;
import to.SeckillSkuRedisTo;
import to.SeckillSkuRelationTo;

import java.util.List;

public interface SeckillService {

    List<Laste3DaysSessionTo> query3DaysSession();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();
}
