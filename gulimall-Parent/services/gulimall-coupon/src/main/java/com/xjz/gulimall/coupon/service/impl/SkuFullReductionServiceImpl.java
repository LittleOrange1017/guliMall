package com.xjz.gulimall.coupon.service.impl;

import com.xjz.gulimall.coupon.entity.MemberPriceEntity;
import com.xjz.gulimall.coupon.entity.SkuLadderEntity;
import com.xjz.gulimall.coupon.service.MemberPriceService;
import com.xjz.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import to.SkuReductionTo;
import utils.PageUtils;
import utils.Query;

import com.xjz.gulimall.coupon.dao.SkuFullReductionDao;
import com.xjz.gulimall.coupon.entity.SkuFullReductionEntity;
import com.xjz.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderService skuLadderService;
    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSkuReduction(SkuReductionTo skuReductionTo, BigDecimal price) {
        //1 保存打折信息
        SkuLadderEntity skuLadderEntity=new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setPrice(price.multiply(skuReductionTo.getDiscount()));
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        skuLadderService.saveSkuLadder(skuLadderEntity);
        //2 保存满减信息
        SkuFullReductionEntity skuFullReductionEntity=new SkuFullReductionEntity();
        skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
        skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
        skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        this.save(skuFullReductionEntity);
        //3 保存会员价格
        List<SkuReductionTo.MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        if(memberPrice!=null&&!memberPrice.isEmpty())
        {
            List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(new Function<SkuReductionTo.MemberPrice, MemberPriceEntity>() {
                @Override
                public MemberPriceEntity apply(SkuReductionTo.MemberPrice memberPrice) {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                    memberPriceEntity.setMemberLevelId(memberPrice.getId());
                    memberPriceEntity.setMemberLevelName(memberPrice.getName());
                    memberPriceEntity.setMemberPrice(memberPrice.getPrice());
                    memberPriceEntity.setAddOther(1);
                    return memberPriceEntity;
                }
            }).filter(item -> {
            // 只有会员价格 > 0 的才需要保存
            return item.getMemberPrice().compareTo(BigDecimal.ZERO) > 0;
        }).collect(Collectors.toList());
            memberPriceService.saveMemberPrice(memberPriceEntities);
        }
    }


}