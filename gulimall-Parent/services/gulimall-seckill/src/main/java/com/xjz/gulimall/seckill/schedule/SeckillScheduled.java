package com.xjz.gulimall.seckill.schedule;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xjz.gulimall.seckill.constants.SeckillRedisConstants;
import com.xjz.gulimall.seckill.feign.CouponFeign;
import com.xjz.gulimall.seckill.feign.ProductFeign;
import com.xjz.gulimall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import to.Laste3DaysSessionTo;
import to.SeckillSkuRedisTo;
import to.SeckillSkuRelationTo;
import to.SkuInfoTo;
import utils.R;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Service
public class SeckillScheduled {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ProductFeign productFeign;


    @Async
    @Scheduled(cron = "0 0 17 ? * SAT")
    public void upload3Days(){
        RLock lock=redissonClient.getLock(SeckillRedisConstants.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            List<Laste3DaysSessionTo> laste3DaysSession = seckillService.query3DaysSession();
            if(laste3DaysSession!=null&&!laste3DaysSession.isEmpty())
            {
                //缓存redis
                saveSessionInfos(laste3DaysSession);
                //缓存SKU详情与信号量
                saveSkuInfos(laste3DaysSession);
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void saveSkuInfos(List<Laste3DaysSessionTo> laste3DaysSession) {
        String key=SeckillRedisConstants.SECKILL_SKUS;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        for (Laste3DaysSessionTo session : laste3DaysSession) {
            //某个场次的所有商品信息
            for (SeckillSkuRelationTo seckillSkuRelationTo : session.getSkuRelationEntityList()) {
                Long sessionId = seckillSkuRelationTo.getPromotionSessionId();
                Long skuId = seckillSkuRelationTo.getSkuId();
                String redisKey = sessionId+"_"+skuId;
                if(Boolean.FALSE.equals(hashOps.hasKey(redisKey)))
                {
                    SeckillSkuRedisTo seckillSkuRedisTo=new SeckillSkuRedisTo();
                    //补全秒杀基本信息
                    BeanUtils.copyProperties(seckillSkuRelationTo,seckillSkuRedisTo);
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    //补全随机码
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(randomCode);
                    //补全商品基本详情
                    SkuInfoTo skuInfo = productFeign.getFeignSkuInfo(skuId);
                    seckillSkuRedisTo.setSkuInfo(skuInfo);
                    //序列化为JSON存入Redis hash
                    hashOps.put(redisKey, JSON.toJSONString(seckillSkuRedisTo));
                    RSemaphore semaphore = redissonClient.getSemaphore(SeckillRedisConstants.SKU_STOCK_SEMAPHORE + randomCode);
                    semaphore.trySetPermits(seckillSkuRelationTo.getSeckillCount().intValue());
                }

            }
        }
    }

    private void saveSessionInfos(List<Laste3DaysSessionTo> laste3DaysSession) {
        for (Laste3DaysSessionTo session : laste3DaysSession) {
            //获取startTime和EndTime
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SeckillRedisConstants.SESSION_SKUS + startTime + "_" + endTime;
            if (!redisTemplate.hasKey(key)) {
                //获取场次ID
                Long sessionId = session.getId();
                List<String> skuIds = session.getSkuRelationEntityList().stream()
                        .map(item -> sessionId + "_" + item.getSkuId())
                        .toList();
                redisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        }
    }
}
