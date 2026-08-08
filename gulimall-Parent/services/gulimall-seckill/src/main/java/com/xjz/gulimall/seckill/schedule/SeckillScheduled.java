package com.xjz.gulimall.seckill.schedule;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xjz.gulimall.seckill.constants.SeckillRedisConstants;
import com.xjz.gulimall.seckill.feign.CouponFeign;
import com.xjz.gulimall.seckill.feign.ProductFeign;
import com.xjz.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SeckillScheduled {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ProductFeign productFeign;

    /**
     * 场次结束后的缓存缓冲期：3天（毫秒），保证缓存存活到下一次定时任务重新上架
     */
    private static final long BUFFER_MILLIS = 3 * 24 * 60 * 60 * 1000L;

    @Async
    @Scheduled(cron = "0 0 14 * * SAT")
    public void upload3Days(){
        RLock lock=redissonClient.getLock(SeckillRedisConstants.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            log.info(">>>>>> 开启定时任务：秒杀商品预热上架开始...");
            List<Laste3DaysSessionTo> laste3DaysSession = seckillService.query3DaysSession();
            if(laste3DaysSession!=null&&!laste3DaysSession.isEmpty())
            {
                //缓存redis
                saveSessionInfos(laste3DaysSession);
                //缓存SKU详情与信号量
                saveSkuInfos(laste3DaysSession);
                log.info(">>>>>> 定时任务：秒杀商品预热上架完成！");
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void saveSkuInfos(List<Laste3DaysSessionTo> laste3DaysSession) {
        String key=SeckillRedisConstants.SECKILL_SKUS;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //记录所有场次中最大的结束时间，用于给共享的hash设置过期
        long maxEndTime = 0;
        boolean hasNewData = false;
        for (Laste3DaysSessionTo session : laste3DaysSession) {
            long sessionEndTime = session.getEndTime().getTime();
            maxEndTime = Math.max(maxEndTime, sessionEndTime);
            //场次过期时间：场次结束 + 3天缓冲
            long expireMillis = sessionEndTime + BUFFER_MILLIS - System.currentTimeMillis();
            if (expireMillis <= 0) {
                continue;
            }
            //某个场次的所有商品信息
            for (SeckillSkuRelationTo seckillSkuRelationTo : session.getSkuRelationEntityList()) {
                Long sessionId = seckillSkuRelationTo.getPromotionSessionId();
                Long skuId = seckillSkuRelationTo.getSkuId();
                String redisKey = sessionId+"_"+skuId;
                if(Boolean.FALSE.equals(hashOps.hasKey(redisKey)))
                {
                    hasNewData = true;
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
                    //信号量跟随场次结束时间过期，避免残留
                    semaphore.expire(expireMillis, TimeUnit.MILLISECONDS);
                }

            }
        }
        //hash被所有场次共享，按最大结束时间统一设置过期
        if (hasNewData && maxEndTime > 0) {
            long hashExpireMillis = maxEndTime + BUFFER_MILLIS - System.currentTimeMillis();
            if (hashExpireMillis > 0) {
                redisTemplate.expire(key, hashExpireMillis, TimeUnit.MILLISECONDS);
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
                //场次结束后缓存过期：结束时间 + 3天缓冲 - 当前时间
                long expireMillis = endTime + BUFFER_MILLIS - System.currentTimeMillis();
                if (expireMillis > 0) {
                    redisTemplate.expire(key, expireMillis, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
