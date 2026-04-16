package com.xjz.gulimall.product.controller;

import com.xjz.gulimall.product.dto.AttrGroupRelationDto;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import com.xjz.gulimall.product.service.AttrGroupService;
import com.xjz.gulimall.product.service.AttrService;
import com.xjz.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import utils.PageUtils;
import utils.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("coupon:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("categoryId") Long categoryId){
        PageUtils page=attrGroupService.queryPage(params,categoryId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("coupon:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogIds=categoryService.findCatelogIds(catelogId);
        attrGroup.setCatelogIds(catelogIds);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除对应组下的属性关联
     */
    @RequestMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrGroupRelationDto> attrGroupRelationDto)
    {
        attrGroupService.deleteRelation(attrGroupRelationDto);
        return R.ok();
    }

    /**
     * 查询对应分组下的属性
     */
    @RequestMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> attrEntity=attrGroupService.attrRelation(attrGroupId);
        return R.ok().put("data",attrEntity);
    }
    /**
     * 查询对应分组下的没有关联的属性
     */
    @RequestMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                           @RequestParam Map<String, Object> params){
        PageUtils page=attrService.attrNoRelation(attrgroupId,params);
        return R.ok().put("data",page);
    }
    /**
     * 添加属性关联
     */
    @RequestMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationDto> attrGroupRelationDto){
        attrGroupService.addRelation(attrGroupRelationDto);
        return R.ok();
    }

}
