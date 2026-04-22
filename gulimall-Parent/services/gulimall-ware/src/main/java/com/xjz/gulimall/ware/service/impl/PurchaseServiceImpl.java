package com.xjz.gulimall.ware.service.impl;

import com.xjz.gulimall.ware.constant.WareConstant;
import com.xjz.gulimall.ware.dto.MergeDto;
import com.xjz.gulimall.ware.entity.PurchaseDetailEntity;
import com.xjz.gulimall.ware.service.PurchaseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private PurchaseDetailService purchaseDetailService;
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
        queryWrapper.eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()).or().eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode());
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void merge(MergeDto mergeDto) {
        Long purchaseId = mergeDto.getPurchaseId();
        List<Long> items = mergeDto.getItems();
        if(items==null||items.size()==0)
        {
            return;
        }
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(items);
        List<Long> wareIds = purchaseDetailEntities.stream().map(new Function<PurchaseDetailEntity, Long>() {
            @Override
            public Long apply(PurchaseDetailEntity purchaseDetailEntity) {
                return purchaseDetailEntity.getWareId();
            }
        }).distinct().collect(Collectors.toList());
        if(wareIds.size()>1)
        {
            throw new RuntimeException("采购商品存在不同的仓库");
        }
        if(purchaseId==null)
        {
            PurchaseEntity purchase=new PurchaseEntity();
            purchase.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchase.setCreateTime(new Date());
            purchase.setUpdateTime(new Date());
            purchase.setWareId(wareIds.get(0));
            this.save(purchase);
            purchaseId=purchase.getId();
        }

        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(new Function<Long, PurchaseDetailEntity>() {
            @Override
            public PurchaseDetailEntity apply(Long aLong) {
                PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                purchaseDetail.setPurchaseId(finalPurchaseId);
                purchaseDetail.setId(aLong);
                purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetail;
            }
        }).collect(Collectors.toList());
        if(collect!=null&&collect.size()>0)
        {
            purchaseDetailService.updateBatchById(collect);
            //更新采购单的最后更新时间
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setWareId(wareIds.get(0));
            this.updateById(purchaseEntity);
        }
    }

    @Override
    public void Mysave(PurchaseEntity purchase) {
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        this.save(purchase);
    }

}