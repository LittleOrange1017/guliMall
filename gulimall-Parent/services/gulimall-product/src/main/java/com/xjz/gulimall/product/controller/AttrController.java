package com.xjz.gulimall.product.controller;

import com.xjz.gulimall.product.dto.AttrDto;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.service.AttrService;
import com.xjz.gulimall.product.vo.AttrInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import utils.PageUtils;
import utils.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 商品属性
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId,@PathVariable("attrType") String attrType)
    {
       PageUtils page= attrService.queryBaseAttrPage(params,catelogId,attrType);
       return R.ok().put("page",page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("coupon:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrInfoVo attrInfoVo = attrService.getInfo(attrId);

        return R.ok().put("attr", attrInfoVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:attr:save")
    public R save(@RequestBody AttrDto attr){
		attrService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:attr:update")
    public R update(@RequestBody AttrDto attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }
    @PostMapping("/getAttrName")
    Map<Long, String> getAttrNamesByIds(@RequestBody List<Long> attrIds)
    {
        List<AttrEntity> attrEntities = attrService.listByIds(attrIds);
        HashMap<Long,String> map=new HashMap<>();
        for (AttrEntity attrEntity : attrEntities) {
            map.put(attrEntity.getAttrId(),attrEntity.getAttrName());
        }
        return map;
    }

}
