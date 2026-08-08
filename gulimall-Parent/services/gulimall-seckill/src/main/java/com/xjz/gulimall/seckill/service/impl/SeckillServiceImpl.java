package com.xjz.gulimall.seckill.service.impl;

import com.xjz.gulimall.seckill.service.SeckillService;
import com.xjz.gulimall.seckill.feign.CouponFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import to.Laste3DaysSessionTo;
import to.SeckillSkuRelationTo;

import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeign couponFeign;

    @Override
    public List<Laste3DaysSessionTo> query3DaysSession() {
        return couponFeign.getLaste3DaysSession();
    }
}
