package com.xjz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import to.OrderStockTo;
import to.SkuStockTo;
import utils.PageUtils;
import com.xjz.gulimall.ware.entity.WareSkuEntity;
import vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:23
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuStockBySpuId(SkuStockTo skuStockTo);

    void lockStock(OrderStockTo orderStockTo);
}

