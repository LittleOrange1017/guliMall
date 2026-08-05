package com.xjz.gulimall.order.mq;

import com.rabbitmq.client.Channel;
import com.xjz.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
public class OrderReleaseListener {
    @Autowired
    private OrderService orderService;
    @RabbitHandler
    public void orderRelease(String orderSn, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.releaseOrder(orderSn);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error(e.toString());
            channel.basicNack(deliveryTag,false,true);
        }

    }


}
