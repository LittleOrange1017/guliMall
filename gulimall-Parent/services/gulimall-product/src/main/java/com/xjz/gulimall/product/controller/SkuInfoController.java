package com.xjz.gulimall.product.controller;

import com.xjz.gulimall.product.entity.SkuInfoEntity;
import com.xjz.gulimall.product.service.SkuInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import to.SkuInfoTo;
import to.SkuWeightTo;
import utils.PageUtils;
import utils.R;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;


/**
 * sku信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:24
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("coupon:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }
    @PostMapping("/getSkuInfo")
    public BigDecimal getSkuInfo(@RequestParam("skuId") Long skuId){
        SkuInfoEntity skuInfoEntity=skuInfoService.getSkuInfo(skuId);
        return skuInfoEntity.getPrice();
    }

    @PostMapping("/skuWeight")
    public Map<Long, BigDecimal> getSkuWeight(@RequestBody SkuWeightTo skuWeightTo)
    {
        return skuInfoService.getSkuWeightBySkuIds(skuWeightTo.getSkuIds());
    }

    @PostMapping("/feign/getSkuInfo/{skuId}")
    public SkuInfoTo getFeignSkuInfo(@PathVariable("skuId") Long skuId){
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        SkuInfoTo skuInfoTo=new SkuInfoTo();
        BeanUtils.copyProperties(skuInfo,skuInfoTo);
        skuInfoTo.setSaleCount(0L);
        return skuInfoTo;
    }

}
