package com.xjz.gulimall.seckill.config;

import com.xjz.gulimall.seckill.constants.SeckillMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeckillMqConfig {
    @Bean
    public TopicExchange orderEventExchange(){
        return ExchangeBuilder.topicExchange(SeckillMqConstants.SECKILL_EXCHANGE).durable(true).build();
    }
    /**
     * 2. 声明秒杀订单队列
     */
    @Bean
    public Queue orderSeckillOrderQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete
        return new Queue(SeckillMqConstants.SECKILL_ORDER_QUEUE, true, false, false);
    }
    /**
     * 3. 将秒杀订单队列绑定到交换机
     */
    @Bean
    public Binding seckillReleaseBinding() {
        return BindingBuilder.bind(orderSeckillOrderQueue()).to(orderEventExchange())
                .with(SeckillMqConstants.SECKILL_ROUTING_KEY);
    }



}
