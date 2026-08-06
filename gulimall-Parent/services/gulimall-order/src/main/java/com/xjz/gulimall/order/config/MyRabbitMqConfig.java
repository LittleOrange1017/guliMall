package com.xjz.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class MyRabbitMqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) {
                if (!ack) {
                    log.error("消息[{}]未到达交换机，原因：{}，需写入本地消息表补偿重投",
                            correlationData != null ? correlationData.getId() : "unknown", cause);
                } else {
                    log.debug("消息[{}]已成功到达交换机", correlationData != null ? correlationData.getId() : "unknown");
                }
            }
        });
    }
}
