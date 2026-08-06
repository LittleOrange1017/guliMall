package com.xjz.gulimall.ware.mq;

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
import java.util.Map;

@Component
@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    private static final int MAX_RETRY = 3;

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(OrderStockTo orderStockTo, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        int retryCount = 0;
        Object retryObj = headers.get("retryCount");
        if (retryObj instanceof Number) {
            retryCount = ((Number) retryObj).intValue();
        }

        try {
            wareSkuService.unLockStock(orderStockTo);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (retryCount < MAX_RETRY) {
                log.warn("库存解锁失败，订单[{}]，第{}次重试，消息重新入队", orderStockTo.getOrderSn(), retryCount + 1, e);
                message.getMessageProperties().getHeaders().put("retryCount", retryCount + 1);
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.error("库存解锁失败，订单[{}]，已达最大重试次数{}，ack跳过，等待兜底对账任务补偿",
                        orderStockTo.getOrderSn(), MAX_RETRY, e);
                channel.basicAck(deliveryTag, false);
            }
        }
    }
}
