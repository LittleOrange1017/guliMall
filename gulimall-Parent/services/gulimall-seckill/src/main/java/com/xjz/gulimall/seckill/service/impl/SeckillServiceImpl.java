package com.xjz.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xjz.gulimall.seckill.constants.SeckillMqConstants;
import com.xjz.gulimall.seckill.constants.SeckillRedisConstants;
import com.xjz.gulimall.seckill.feign.ProductFeign;
import com.xjz.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.xjz.gulimall.seckill.service.SeckillService;
import com.xjz.gulimall.seckill.feign.CouponFeign;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import to.Laste3DaysSessionTo;
import to.SeckillOrderTo;
import to.SeckillSkuRedisTo;
import to.SkuInfoTo;
import utils.R;
import vo.MemberVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeign couponFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<Laste3DaysSessionTo> query3DaysSession() {
        return couponFeign.getLaste3DaysSession();
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //获取当前时间
        long now = System.currentTimeMillis();
        //查找redis中所有秒杀场次的Key seckill:sessions:start_end
        String pattenKey= SeckillRedisConstants.SESSION_SKUS+"*";
        Set<String> keys = redisTemplate.keys(pattenKey);
        if(keys!=null&&!keys.isEmpty())
        {
            //获取商品详细信息，查询第二种缓存的数据，移出循环只构造一次
            BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillRedisConstants.SECKILL_SKUS);
            for (String key : keys) {
                //匹配到的key，我们要提取出末尾的start_end字符串
                //key样例：seckill:sessions:1770508800000_1770516000000
                String replace = key.replace(SeckillRedisConstants.SESSION_SKUS, "");
                //replace样例：17705080000_177051600000
                String[] split = replace.split("_");
                long startTime;
                long endTime;
                try {
                    //split[0]:startTime;split[1]:endTime
                    startTime = Long.parseLong(split[0]);
                    endTime = Long.parseLong(split[1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    //key格式非法时跳过该场次，避免影响整体查询
                    continue;
                }
                //判断是否处于当前秒杀时间段内
                if(now>=startTime&&now<=endTime)
                {
                    //获取该场次下的所有session_skuId标识，也即获得该key的value集合
                    List<String> value = redisTemplate.opsForList().range(key, 0, -1);
                    //value示例：["1_10","1_11"......]
                    if(value!=null&&!value.isEmpty())
                    {
                        //hMGet一次性批量获取，一次Redis往返，null即表示该hashKey不存在（含已过期）
                        List<String> values = hashOps.multiGet(value);
                        if(values==null)
                        {
                            return null;
                        }
                        return values.stream()
                                .filter(StringUtils::hasText)
                                .map(o -> JSON.parseObject(o, SeckillSkuRedisTo.class))
                                .collect(Collectors.toList());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillRedisConstants.SECKILL_SKUS);
        Set<String> keys = hashOps.keys();
        //匹配后缀为_skuId，例如1_10匹配_10
        String regx = "\\d_[" + skuId + "]";
        for (String key : keys) {
            if (Pattern.matches(regx, key) || key.endsWith("_" + skuId)) {
                //匹配到了对应的key，查询出对应的value
                String json = hashOps.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                if(redisTo!=null)
                {
                    long now = System.currentTimeMillis();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    //如果当前时间是秒杀进行时
                    if(now>=startTime&&now<=endTime)
                    {
                        return redisTo;
                    }
                    //如果秒杀尚未开始，隐藏随机码
                    else if(now < startTime)
                    {
                        redisTo.setRandomCode(null);
                        return redisTo;
                    }
                    // 3. 秒杀已经结束：返回 null
                    else {
                        return null;
                    }
                }
            }
        }
        return null;

    }

    @Override
    public R kill(String killId, String key, Integer num) {
        //1、校验登录状态
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        if(memberVo==null)
        {
            log.warn("用户未登录");
            return R.error(401, "请先登录后再参与秒杀");
        }
        //2、校验时间
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SeckillRedisConstants.SECKILL_SKUS);
        String json = hashOps.get(killId);
        if(!StringUtils.hasText(json))
        {
            return R.error(400, "该商品秒杀信息不存在或已过期");
        }
        SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
        //获取场次时间
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        long now = System.currentTimeMillis();
        if (now < startTime || now > endTime) {
            log.warn("抢购失败，非活动时间段！now:{}, start:{}, end:{}", now, startTime, endTime);
            return R.error(400, "秒杀活动尚未开始或已经结束");
        }
        //3、防刷随机码校验
        String randomCode = redisTo.getRandomCode();
        if(!key.equals(randomCode))
        {
            log.warn("抢购失败，防刷随机码不匹配！killId:{}", killId);
            return R.error(400, "秒杀信息已失效，请刷新页面重试");
        }
        //4、购买数量校验
        int limit = redisTo.getSeckillLimit().intValue();
        if(num>limit)
        {
            log.warn("抢购失败，超出单人最大限购数量！num:{}, limit:{}", num, redisTo.getSeckillLimit());
            return R.error(400, "每人限购" + limit + "件，不能超出限购数量");
        }
        //5、单人重复抢购校验
        String userKey = "seckill:user:" + memberVo.getUserId() + "_" + killId;
        // 占位有效时长 = 秒杀活动剩余时间 (活动结束自动释放，释放 Redis 内存)
        long ttl = endTime - now;
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(userKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(setIfAbsent)) {
            // 如果 SETNX 失败，说明该用户已经抢购过该商品！
            log.warn("抢购失败，用户 [id={}] 已经参与过该商品的秒杀！", memberVo.getUserId());
            return R.error(400, "您已参与过该商品的秒杀，请勿重复抢购");
        }
        //6、RSemaphore 信号量扣减
        RSemaphore semaphore = redissonClient.getSemaphore(SeckillRedisConstants.SKU_STOCK_SEMAPHORE + randomCode);
        boolean acquire=false;
        try {
            acquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("信号量扣减异常", e);
            return R.error(500, "系统繁忙，请稍后重试");
        }

        if (!acquire) {
            log.warn("抢购失败，库存不足/信号量扣减失败！killId:{}", killId);
            return R.error(400, "手慢了，秒杀商品已抢光");
        }
        //此时用户已经抢购成功，组装消息对象，发往消息队列。
        //1、生成全局唯一订单号（与普通订单格式一致：userId + 时间戳 + 随机数）
        String orderSn = memberVo.getUserId().toString()
                + System.currentTimeMillis()
                + (int)(Math.random() * 1000);
        //2、组装消息对象
        SeckillOrderTo seckillOrderTo=new SeckillOrderTo();
        seckillOrderTo.setOrderSn(orderSn);
        seckillOrderTo.setNum(num);
        seckillOrderTo.setMemberId(memberVo.getUserId());
        seckillOrderTo.setSeckillPrice(redisTo.getSeckillPrice());
        seckillOrderTo.setSkuId(redisTo.getSkuId());
        seckillOrderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
        //3、发送消息至 RabbitMQ
        rabbitTemplate.convertAndSend(SeckillMqConstants.SECKILL_EXCHANGE,SeckillMqConstants.SECKILL_ROUTING_KEY,seckillOrderTo);
        log.info("恭喜！用户 [id={}] 秒杀成功！订单号:{}, 消息已投递至 RabbitMQ！", memberVo.getUserId(), orderSn);

        // 4. 返回订单号，整个秒杀耗时约 10~20ms！
        return R.ok("秒杀成功").put("orderSn", orderSn);
    }
}
