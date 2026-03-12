package com.xjz.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:59:14
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

