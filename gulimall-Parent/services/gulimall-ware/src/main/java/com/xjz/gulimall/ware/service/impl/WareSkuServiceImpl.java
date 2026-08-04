package com.xjz.gulimall.ware.service.impl;

import com.xjz.gulimall.ware.entity.WareInfoEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import to.OrderStockTo;
import to.SkuStockLockedTo;
import to.SkuStockTo;
import utils.PageUtils;
import utils.Query;


import com.xjz.gulimall.ware.dao.WareSkuDao;
import com.xjz.gulimall.ware.entity.WareSkuEntity;
import com.xjz.gulimall.ware.service.WareSkuService;
import vo.SkuHasStockVo;



@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
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
    public void lockStock(OrderStockTo orderStockTo) {
        for (SkuStockLockedTo lock : orderStockTo.getLocks()) {
            int rows = wareSkuDao.lockStock(lock.getSkuId(), lock.getSkuNum());
            if(rows==0)
            {
                throw new RuntimeException("商品SKU[" + lock.getSkuId() + "]库存不足，锁定失败");
            }
        }

    }

}