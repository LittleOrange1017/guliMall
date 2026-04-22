package com.xjz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.ware.entity.PurchaseDetailEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-04-20 15:04:30
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void savePurchaseDetail(PurchaseDetailEntity purchaseDetail);
}

