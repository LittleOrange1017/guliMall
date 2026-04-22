package com.xjz.gulimall.ware.service.impl;

import com.xjz.gulimall.ware.constant.WareConstant;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xjz.gulimall.ware.dao.PurchaseDao;
import com.xjz.gulimall.ware.entity.PurchaseEntity;
import com.xjz.gulimall.ware.service.PurchaseService;
import utils.PageUtils;
import utils.Query;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED).or().eq("status", WareConstant.PurchaseStatusEnum.CREATED);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

}