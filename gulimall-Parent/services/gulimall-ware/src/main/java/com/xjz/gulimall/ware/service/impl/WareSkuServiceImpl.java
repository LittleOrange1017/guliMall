package com.xjz.gulimall.ware.service.impl;

import com.xjz.gulimall.ware.entity.WareInfoEntity;
import com.xjz.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.xjz.gulimall.ware.entity.WareOrderTaskEntity;
import com.xjz.gulimall.ware.feign.OrderFeign;
import com.xjz.gulimall.ware.service.WareOrderTaskDetailService;
import com.xjz.gulimall.ware.service.WareOrderTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import to.OrderStockTo;
import to.SkuStockLockedTo;
import to.SkuStockTo;
import utils.PageUtils;
import utils.Query;


import com.xjz.gulimall.ware.dao.WareSkuDao;
import com.xjz.gulimall.ware.entity.WareSkuEntity;
import com.xjz.gulimall.ware.service.WareSkuService;
import utils.R;
import vo.SkuHasStockVo;



@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private WareOrderTaskService taskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private OrderFeign orderFeign;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper=new QueryWrapper<>();
        String wareId=(String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)&&!wareId.equals("0"))
        {
            queryWrapper.eq("ware_id",wareId);
        }
        String skuId=(String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)&&!skuId.equals("0"))
        {
            queryWrapper.eq("sku_id",skuId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        wareSkuDao.addStock(skuId,wareId,skuNum);
    }

    @Override
    public List<SkuHasStockVo> getSkuStockBySpuId(SkuStockTo skuStockTo) {
        List<Long> skuIdList = skuStockTo.getSkuId();
        // 使用 sku_id 字段查询，而不是主键 id
        List<WareSkuEntity> wareSkuEntities = this.list(
                new QueryWrapper<WareSkuEntity>().in("sku_id", skuIdList)
        );
        return wareSkuEntities.stream().map(new Function<WareSkuEntity, SkuHasStockVo>() {
            @Override
            public SkuHasStockVo apply(WareSkuEntity wareSkuEntity) {
                SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
                Long skuId = wareSkuEntity.getSkuId();
                Boolean hasStock = wareSkuEntity.getStock() > 0;
                skuHasStockVo.setSkuId(skuId);
                skuHasStockVo.setHasStock(hasStock);
                return skuHasStockVo;
            }
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void lockStock(OrderStockTo orderStockTo) {
        WareOrderTaskEntity task=new WareOrderTaskEntity();
        task.setOrderSn(orderStockTo.getOrderSn());
        task.setCreateTime(new Date());
        task.setOrderId(orderStockTo.getOrderId());
        List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities=new ArrayList<>();
        //保存工作单
        taskService.save(task);
        for (SkuStockLockedTo lock : orderStockTo.getLocks()) {
            List<WareSkuEntity> skuEntities = wareSkuDao.listLockStock(lock.getSkuId(), lock.getSkuNum());
            if (skuEntities == null || skuEntities.isEmpty()) {
                throw new RuntimeException("商品SKU[" + lock.getSkuId() + "]库存不足，锁定失败");
            }
            WareSkuEntity wareSkuEntity = skuEntities.get(0);
            int rows = wareSkuDao.lockStock(lock.getSkuId(), lock.getSkuNum());
            if(rows==0)
            {
                throw new RuntimeException("商品SKU[" + lock.getSkuId() + "]库存不足，锁定失败");
            }
            WareOrderTaskDetailEntity detail=new WareOrderTaskDetailEntity();
            detail.setSkuId(lock.getSkuId());
            detail.setSkuName(wareSkuEntity.getSkuName());
            detail.setSkuNum(lock.getSkuNum());
            detail.setTaskId(task.getId());
            detail.setWareId(wareSkuEntity.getWareId());
            detail.setLockStatus(1);
            wareOrderTaskDetailEntities.add(detail);
        }
        wareOrderTaskDetailService.saveBatch(wareOrderTaskDetailEntities);
    }

    /**
     * 库存解锁（由 31 分钟延迟消息到期触发）
     * 流程：
     * 1、按 order_sn 查工作单，查不到说明数据异常，记录错误并返回（避免消息无限重投）；
     * 2、只查 lock_status = 1 的明细——幂等核心，已解锁订单查不到待处理明细直接返回；
     * 3、RPC 反查订单状态决策：
     *    - 订单不存在(-1) / 已关闭(4)：执行解锁，明细流转为 2 并持久化；
     *    - 仍待付款(0)：时序竞态，主动抛异常让消息重新入队；
     *    - 已付款及之后状态(1/2/3)：跳过，不动库存。
     * 异常说明：待付款抛 RuntimeException、RPC 失败抛 ExecutionException，
     * 均由监听器捕获后 basicNack 重新入队。
     */
    @Override
    @Transactional
    public void unLockStock(OrderStockTo orderStockTo) throws ExecutionException, InterruptedException {
        String orderSn = orderStockTo.getOrderSn();
        WareOrderTaskEntity task = taskService.getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        if(task==null)
        {
            log.error("订单[{}]不存在对应的库存工作单，消息异常，跳过处理", orderSn);
            return;
        }
        List<WareOrderTaskDetailEntity> details = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", task.getId())
                        .eq("lock_status", 1));
        if (details == null || details.isEmpty()) {
            log.info("订单[{}]的工作单明细已全部释放，幂等跳过", orderSn);
            return;
        }
        CompletableFuture<R> async = CompletableFuture.supplyAsync(new Supplier<R>() {
            @Override
            public R get() {
                return orderFeign.getOrderStatus(orderSn);
            }
        }, threadPoolExecutor);
        R r = async.get();
        Integer status = r == null ? null : (Integer) r.get("status");

        if (status == null || status == -1 || status == 4) {
            for(WareOrderTaskDetailEntity detail:details)
            {
                wareSkuDao.unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
                detail.setLockStatus(2);
            }
            wareOrderTaskDetailService.updateBatchById(details);
        }
        else if (status == 0) {
            throw new RuntimeException("订单[" + orderSn + "]仍为待付款状态，关单流程未完成，消息重新入队等待");
        }
        else
        {
            log.info("订单[{}]已付款，跳过库存解锁", orderSn);
        }
    }
}