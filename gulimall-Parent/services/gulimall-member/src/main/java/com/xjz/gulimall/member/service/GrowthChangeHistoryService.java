package com.xjz.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.member.entity.GrowthChangeHistoryEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:59:14
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

