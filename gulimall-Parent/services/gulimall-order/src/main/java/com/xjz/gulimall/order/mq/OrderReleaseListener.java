package com.xjz.gulimall.order.mq;

import com.rabbitmq.client.Channel;
import com.xjz.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
public class OrderReleaseListener {
    private static final String REDIS_RETRY_KEY_PREFIX = "order:release:retry:";
    private static final int MAX_RETRY=3;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @RabbitHandler
    public void orderRelease(String orderSn, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String redisRetryKey=REDIS_RETRY_KEY_PREFIX+orderSn;
        try {
            orderService.releaseOrder(orderSn);
            channel.basicAck(deliveryTag,false);
            redisTemplate.delete(redisRetryKey);
        } catch (Exception e) {
            Long currentRetryCount = redisTemplate.opsForValue().increment(redisRetryKey);
            redisTemplate.expire(redisRetryKey, 1, TimeUnit.HOURS);
            if (currentRetryCount != null && currentRetryCount <= MAX_RETRY) {
                log.warn("超时关单失败，订单[{}]，第 {} 次重试，消息重新入队: {}",
                        orderSn, currentRetryCount, e.getMessage());
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.error("超时关单失败，订单[{}]已达最大重试次数[{}]！ACK 弹出消息，交由订单定时任务兜底对账",
                        orderSn, MAX_RETRY, e);
                // 弹出毒消息，依靠 OrderCloseTask 定时任务兜底
                channel.basicAck(deliveryTag, false);
                redisTemplate.delete(redisRetryKey);
            }
        }

    }


}
