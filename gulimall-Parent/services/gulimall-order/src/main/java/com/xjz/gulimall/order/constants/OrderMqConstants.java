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

    /** ==================== 积分锁定超时回退 ==================== */
    public static final String INTEGRATION_DELAY_QUEUE = "integration.delay.queue";
    public static final String INTEGRATION_RELEASE_QUEUE = "integration.release.integration.queue";
    public static final String INTEGRATION_LOCK_ROUTING_KEY = "integration.locked.integration";
    public static final String INTEGRATION_RELEASE_ROUTING_KEY = "integration.release.integration";

    /** TTL 配置：订单关单 30 分钟，库存/积分释放 31 分钟 */
    public static final Integer ORDER_TTL = 30 * 60 * 1000;
    public static final Integer STOCK_TTL = 31 * 60 * 1000;
    public static final Integer INTEGRATION_TTL = 31 * 60 * 1000;
}

