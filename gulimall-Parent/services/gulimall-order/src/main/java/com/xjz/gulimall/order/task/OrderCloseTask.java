package com.xjz.gulimall.order.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xjz.gulimall.order.entity.OrderEntity;
import com.xjz.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class OrderCloseTask {
    private static final long TIMEOUT_MILLS=30*60*1000L;
    @Autowired
    private OrderService orderService;
    @Async
    @Scheduled(fixedDelay = 5*60*1000)
    public void closeOverdueOrders(){
        //截止日期
        Date untilDate=new Date(System.currentTimeMillis()-TIMEOUT_MILLS);
        //查询超时订单
        List<OrderEntity> list = orderService.list(new QueryWrapper<OrderEntity>().eq("status", 0).lt("create_time", untilDate));
        if(list==null||list.isEmpty())
        {
            //无超时订单，直接返回
            return;
        }
        //发现超时订单
        for(OrderEntity order:list)
        {
            try {
                orderService.releaseOrder(order.getOrderSn());
            } catch (Exception e) {
                log.error("兜底对账：关单失败，订单[{}]", order.getOrderSn(), e);
            }
        }
    }
}
