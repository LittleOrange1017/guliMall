package com.xjz.gulimall.ware.config;

import com.xjz.gulimall.ware.constant.WareMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WareMqConfig {
    /**
     * 声明订单事件交换机（Topic 类型，持久化）
     */
    @Bean
    public TopicExchange orderEventExchange() {
        return ExchangeBuilder.topicExchange(WareMqConstants.ORDER_EXCHANGE).durable(true).build();
    }

    /**
     * 订单延迟队列（TTL=30min）
     * 消息过期后通过 DLX 路由到 order.release.order.queue
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", WareMqConstants.ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", WareMqConstants.ORDER_RELEASE_ROUTING_KEY);
        args.put("x-message-ttl", WareMqConstants.ORDER_TTL);
        return QueueBuilder.durable(WareMqConstants.ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 订单释放队列（实际消费队列）
     */
    @Bean
    public Queue orderReleaseQueue() {
        return QueueBuilder.durable(WareMqConstants.ORDER_RELEASE_QUEUE).build();
    }

    /**
     * 库存延迟队列（TTL=31min）
     * 消息过期后通过 DLX 路由到 stock.release.stock.queue
     */
    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", WareMqConstants.ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", WareMqConstants.STOCK_RELEASE_ROUTING_KEY);
        args.put("x-message-ttl", WareMqConstants.STOCK_TTL);
        return QueueBuilder.durable(WareMqConstants.STOCK_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 库存释放队列（实际消费队列，由 ware 服务监听）
     */
    @Bean
    public Queue stockReleaseQueue() {
        return QueueBuilder.durable(WareMqConstants.STOCK_RELEASE_QUEUE).build();
    }


    /** ==================== 绑定关系 ==================== */


    /** 库存锁定消息 → 库存延迟队列 */
    @Bean
    public Binding stockLockBinding() {
        return BindingBuilder.bind(stockDelayQueue()).to(orderEventExchange())
                .with(WareMqConstants.STOCK_LOCK_ROUTING_KEY);
    }

    /** 库存释放消息 → 库存释放队列 */
    @Bean
    public Binding stockReleaseBinding() {
        return BindingBuilder.bind(stockReleaseQueue()).to(orderEventExchange())
                .with(WareMqConstants.STOCK_RELEASE_ROUTING_KEY);
    }
}
