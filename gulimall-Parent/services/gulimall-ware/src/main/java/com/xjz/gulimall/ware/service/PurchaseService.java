package com.xjz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.ware.dto.MergeDto;
import com.xjz.gulimall.ware.entity.PurchaseEntity;
import utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-04-20 15:04:30
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void merge(MergeDto mergeDto);

    void Mysave(PurchaseEntity purchase);

    void received(List<Long> ids);
}

