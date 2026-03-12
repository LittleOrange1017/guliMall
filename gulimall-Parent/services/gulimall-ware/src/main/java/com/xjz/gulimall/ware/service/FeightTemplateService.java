package com.xjz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.ware.entity.FeightTemplateEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 运费模板
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:23
 */
public interface FeightTemplateService extends IService<FeightTemplateEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

