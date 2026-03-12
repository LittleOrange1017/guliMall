package com.xjz.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xjz.gulimall.ware.entity.ShAreaEntity;
import com.xjz.gulimall.ware.service.ShAreaService;
import utils.PageUtils;
import utils.R;



/**
 * 全国省市区信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:24
 */
@RestController
@RequestMapping("ware/sharea")
public class ShAreaController {
    @Autowired
    private ShAreaService shAreaService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:sharea:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = shAreaService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:sharea:info")
    public R info(@PathVariable("id") Integer id){
		ShAreaEntity shArea = shAreaService.getById(id);

        return R.ok().put("shArea", shArea);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:sharea:save")
    public R save(@RequestBody ShAreaEntity shArea){
		shAreaService.save(shArea);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:sharea:update")
    public R update(@RequestBody ShAreaEntity shArea){
		shAreaService.updateById(shArea);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:sharea:delete")
    public R delete(@RequestBody Integer[] ids){
		shAreaService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
