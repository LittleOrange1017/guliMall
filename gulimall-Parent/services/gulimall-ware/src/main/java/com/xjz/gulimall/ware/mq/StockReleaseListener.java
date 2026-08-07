package com.xjz.gulimall.ware.mq;

import com.rabbitmq.client.Channel;
import com.xjz.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import to.OrderStockTo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    private static final int MAX_RETRY = 3;
    private static final String REDIS_RETRY_KEY_PREFIX = "stock:release:retry:";

    @Autowired
    private WareSkuService wareSkuService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitHandler
    public void handleStockLockedRelease(OrderStockTo orderStockTo, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String orderSn = orderStockTo.getOrderSn();
        String redisRetryKey=REDIS_RETRY_KEY_PREFIX+orderSn;
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        try {
            wareSkuService.unLockStock(orderStockTo);
            channel.basicAck(deliveryTag, false);
            stringRedisTemplate.delete(redisRetryKey);
        } catch (Exception e) {
            //解锁异常
            Long currentRetryCount = stringRedisTemplate.opsForValue().increment(redisRetryKey);
            // 给 Redis Key 设置 1 小时 TTL，防止垃圾 Key 长期残留占用内存
            stringRedisTemplate.expire(redisRetryKey, 1, TimeUnit.HOURS);
            if(currentRetryCount!=null&&currentRetryCount<=MAX_RETRY)
            {
                //还继续重试
                channel.basicNack(deliveryTag, false, true);
            }
            else
            {
                channel.basicAck(deliveryTag, false);
                stringRedisTemplate.delete(redisRetryKey);
            }
        }
    }
}
