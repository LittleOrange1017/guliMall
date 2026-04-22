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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.PageUtils;
import utils.Query;
import utils.R;


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
    @Transactional
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

    @Transactional // 同样涉及两张表的修改，必须开启事务
    @Override
    public void received(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }

        // 1. 确认当前采购单是【新建】或【已分配】状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            // 获取数据库中最新的采购单数据
            return this.getById(id);
        }).filter(item -> {
            // 防御校验：只允许状态为 0 或 1 的通过
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).map(item -> {
            // 组装要更新的实体对象（只更新状态和时间，提升性能并防止并发覆盖）
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(item.getId());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            purchaseEntity.setUpdateTime(new Date());
            return purchaseEntity;
        }).collect(Collectors.toList());

        // 2. 批量更新主采购单状态
        if (collect != null && collect.size() > 0) {
            this.updateBatchById(collect);

            // 3. 级联更新子表（采购需求单）的状态
            collect.forEach(item -> {
                // 3.1 查出当前采购单下的所有采购需求
                // 注意：这里需要在 PurchaseDetailService 中提供一个根据 purchaseId 查出所有明细的方法
                List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());

                // 3.2 组装子表更新对象
                List<PurchaseDetailEntity> detailCollect = detailEntities.stream().map(detail -> {
                    PurchaseDetailEntity entity = new PurchaseDetailEntity();
                    entity.setId(detail.getId());
                    // 状态扭转为 "正在采购"
                    entity.setStatus(WareConstant.PurchaseDetailStatusEnum.RECEIVE.getCode());
                    return entity;
                }).collect(Collectors.toList());

                // 3.3 批量更新子表
                purchaseDetailService.updateBatchById(detailCollect);
            });
        }
    }

}