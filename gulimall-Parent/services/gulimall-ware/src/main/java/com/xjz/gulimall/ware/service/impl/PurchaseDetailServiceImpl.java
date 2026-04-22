package com.xjz.gulimall.ware.service.impl;

import com.xjz.gulimall.ware.feign.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.xjz.gulimall.ware.dao.PurchaseDetailDao;
import com.xjz.gulimall.ware.entity.PurchaseDetailEntity;
import com.xjz.gulimall.ware.service.PurchaseDetailService;
import utils.PageUtils;
import utils.Query;
import utils.R;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void savePurchaseDetail(PurchaseDetailEntity purchaseDetail) {
        Long skuId = purchaseDetail.getSkuId();
        BigDecimal price = productFeignClient.getSkuInfo(skuId);
        BigDecimal skuNum = BigDecimal.valueOf(purchaseDetail.getSkuNum());
        purchaseDetail.setSkuPrice(price.multiply(skuNum));
        this.save(purchaseDetail);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id",id));
    }

}