package com.xjz.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xjz.gulimall.ware.entity.FeightTemplateEntity;
import com.xjz.gulimall.ware.service.FeightTemplateService;
import utils.PageUtils;
import utils.R;


/**
 * 运费模板
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:23
 */
@RestController
@RequestMapping("ware/feighttemplate")
public class FeightTemplateController {
    @Autowired
    private FeightTemplateService feightTemplateService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:feighttemplate:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = feightTemplateService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:feighttemplate:info")
    public R info(@PathVariable("id") Long id){
		FeightTemplateEntity feightTemplate = feightTemplateService.getById(id);

        return R.ok().put("feightTemplate", feightTemplate);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:feighttemplate:save")
    public R save(@RequestBody FeightTemplateEntity feightTemplate){
		feightTemplateService.save(feightTemplate);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:feighttemplate:update")
    public R update(@RequestBody FeightTemplateEntity feightTemplate){
		feightTemplateService.updateById(feightTemplate);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:feighttemplate:delete")
    public R delete(@RequestBody Long[] ids){
		feightTemplateService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
