package com.xjz.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.seckill.constants.SeckillRedisConstants;
import com.xjz.gulimall.seckill.feign.ProductFeign;
import com.xjz.gulimall.seckill.service.SeckillService;
import com.xjz.gulimall.seckill.feign.CouponFeign;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import to.Laste3DaysSessionTo;
import to.SeckillSkuRedisTo;
import to.SkuInfoTo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeign couponFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeign productFeign;

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
}
