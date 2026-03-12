package com.xjz.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xjz.gulimall.coupon.entity.SpuLadderEntity;
import com.xjz.gulimall.coupon.service.SpuLadderService;
import utils.PageUtils;
import utils.R;




/**
 * 商品阶梯价格
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:22
 */
@RestController
@RequestMapping("coupon/spuladder")
public class SpuLadderController {
    @Autowired
    private SpuLadderService spuLadderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:spuladder:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuLadderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:spuladder:info")
    public R info(@PathVariable("id") Long id){
		SpuLadderEntity spuLadder = spuLadderService.getById(id);

        return R.ok().put("spuLadder", spuLadder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:spuladder:save")
    public R save(@RequestBody SpuLadderEntity spuLadder){
		spuLadderService.save(spuLadder);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:spuladder:update")
    public R update(@RequestBody SpuLadderEntity spuLadder){
		spuLadderService.updateById(spuLadder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:spuladder:delete")
    public R delete(@RequestBody Long[] ids){
		spuLadderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
