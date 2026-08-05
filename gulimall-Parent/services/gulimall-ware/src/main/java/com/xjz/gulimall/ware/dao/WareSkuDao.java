package com.xjz.gulimall.ware.dao;

import com.xjz.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:23
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(Long skuId, Long wareId, Integer skuNum);
    int lockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum,@Param("wareId") Long wareId);
    /**
     * 查询指定 SKU 可锁定的仓库记录（stock - stock_locked >= 需求量）
     */
    List<WareSkuEntity> listLockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    int unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
