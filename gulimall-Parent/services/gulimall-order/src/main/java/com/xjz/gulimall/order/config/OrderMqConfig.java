package com.xjz.gulimall.order.config;

import com.xjz.gulimall.order.constants.OrderMqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.xjz.gulimall.order.constants.OrderMqConstants.*;

@Configuration
public class OrderMqConfig {

    /**
     * 声明订单事件交换机（Topic 类型，持久化）
     */
    @Bean
    public TopicExchange orderEventExchange() {
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).durable(true).build();
    }

    /**
     * 订单延迟队列（TTL=30min）
     * 消息过期后通过 DLX 路由到 order.release.order.queue
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", ORDER_RELEASE_ROUTING_KEY);
        args.put("x-message-ttl", ORDER_TTL);
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 订单释放队列（实际消费队列）
     */
    @Bean
    public Queue orderReleaseQueue() {
        return QueueBuilder.durable(ORDER_RELEASE_QUEUE).build();
    }

    /**
     * 库存延迟队列（TTL=31min）
     * 消息过期后通过 DLX 路由到 stock.release.stock.queue
     */
    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", STOCK_RELEASE_ROUTING_KEY);
        args.put("x-message-ttl", STOCK_TTL);
        return QueueBuilder.durable(STOCK_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 库存释放队列（实际消费队列，由 ware 服务监听）
     */
    @Bean
    public Queue stockReleaseQueue() {
        return QueueBuilder.durable(STOCK_RELEASE_QUEUE).build();
    }


    /** ==================== 绑定关系 ==================== */

    /** 订单创建消息 → 订单延迟队列 */
    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderEventExchange())
                .with(ORDER_CREATE_ROUTING_KEY);
    }

    /** 订单释放消息 → 订单释放队列 */
    @Bean
    public Binding orderReleaseBinding() {
        return BindingBuilder.bind(orderReleaseQueue()).to(orderEventExchange())
                .with(ORDER_RELEASE_ROUTING_KEY);
    }

    /** 库存锁定消息 → 库存延迟队列 */
    @Bean
    public Binding stockLockBinding() {
        return BindingBuilder.bind(stockDelayQueue()).to(orderEventExchange())
                .with(STOCK_LOCK_ROUTING_KEY);
    }

    /** 库存释放消息 → 库存释放队列 */
    @Bean
    public Binding stockReleaseBinding() {
        return BindingBuilder.bind(stockReleaseQueue()).to(orderEventExchange())
                .with(STOCK_RELEASE_ROUTING_KEY);
    }
}

