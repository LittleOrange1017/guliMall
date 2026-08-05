package com.xjz.gulimall.ware.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.xjz.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import to.OrderStockTo;

import java.io.IOException;

@Component
@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    private WareSkuService wareSkuService;
    @RabbitHandler
    public void handleStockLockedRelease(OrderStockTo orderStockTo, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try{
            wareSkuService.unLockStock(orderStockTo);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("库存解锁消息处理失败，订单号[{}]，消息重新入队", orderStockTo.getOrderSn(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
