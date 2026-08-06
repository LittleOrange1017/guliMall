package com.xjz.gulimall.ware.task;

import com.xjz.gulimall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WareUnlockTask {
    private static final int TIMEOUT_MINUTES = 35;
    @Autowired
    private WareSkuService wareSkuService;
    @Async
    @Scheduled(fixedDelay = 5*60*1000)
    public void reconcile(){
        //开始扫描超过35分钟的未释放工作单
        try {
            wareSkuService.reconcileLockedStock(TIMEOUT_MINUTES);
        } catch (Exception e) {
            log.error("库存兜底对账异常", e);
        }
    }
}
