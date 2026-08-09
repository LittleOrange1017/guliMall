package com.xjz.gulimall.order.mq;

import com.rabbitmq.client.Channel;
import com.xjz.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import to.SeckillOrderTo;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info(">>>>>> 收到秒杀订单 MQ 异步建单消息，订单号: {}", seckillOrderTo.getOrderSn());

        try {
            // 执行后台落库建单逻辑
            orderService.createSeckillOrder(seckillOrderTo);
            // 手动 ACK 确认消费成功
            channel.basicAck(deliveryTag, false);
            log.info(">>>>>> 秒杀订单建单完成，消息 ACK 成功！订单号: {}", seckillOrderTo.getOrderSn());

        } catch (Exception e) {
            log.error(">>>>>> 秒杀订单落库异常，订单号: {}", seckillOrderTo.getOrderSn(), e);
            // 消费失败，拒绝消息，true 表示重新放回队列重试
            channel.basicReject(deliveryTag, true);
        }
    }
}
