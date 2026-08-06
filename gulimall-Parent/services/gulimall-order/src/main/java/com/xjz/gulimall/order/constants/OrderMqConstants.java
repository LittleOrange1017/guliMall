package com.xjz.gulimall.order.constants;

public class OrderMqConstants {

    /** 订单事件交换机（Topic 类型） */
    public static final String ORDER_EXCHANGE = "order-event-exchange";

    /** ==================== 订单超时关单 ==================== */
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_RELEASE_QUEUE = "order.release.order.queue";
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create.order";
    public static final String ORDER_RELEASE_ROUTING_KEY = "order.release.order";

    /** ==================== 库存锁定超时解锁 ==================== */
    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";
    public static final String STOCK_RELEASE_QUEUE = "stock.release.stock.queue";
    public static final String STOCK_LOCK_ROUTING_KEY = "stock.locked.stock";
    public static final String STOCK_RELEASE_ROUTING_KEY = "stock.release.stock";


    /** TTL 配置（测试用，上线前改回 30*60*1000 / 31*60*1000） */
    public static final Integer ORDER_TTL = 30*60 * 1000;
    public static final Integer STOCK_TTL = 31*60 * 1000;
}

