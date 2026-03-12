package com.xjz.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xjz.gulimall.coupon.entity.SpuFullReductionEntity;
import com.xjz.gulimall.coupon.service.SpuFullReductionService;
import utils.PageUtils;
import utils.R;




/**
 * 商品满减信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:22
 */
@RestController
@RequestMapping("coupon/spufullreduction")
public class SpuFullReductionController {
    @Autowired
    private SpuFullReductionService spuFullReductionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:spufullreduction:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuFullReductionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:spufullreduction:info")
    public R info(@PathVariable("id") Long id){
		SpuFullReductionEntity spuFullReduction = spuFullReductionService.getById(id);

        return R.ok().put("spuFullReduction", spuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:spufullreduction:save")
    public R save(@RequestBody SpuFullReductionEntity spuFullReduction){
		spuFullReductionService.save(spuFullReduction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:spufullreduction:update")
    public R update(@RequestBody SpuFullReductionEntity spuFullReduction){
		spuFullReductionService.updateById(spuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:spufullreduction:delete")
    public R delete(@RequestBody Long[] ids){
		spuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
