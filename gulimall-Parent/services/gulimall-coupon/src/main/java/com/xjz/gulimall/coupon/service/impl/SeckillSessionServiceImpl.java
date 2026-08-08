package com.xjz.gulimall.coupon.service.impl;

import com.xjz.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.xjz.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.PageUtils;
import utils.Query;
import com.xjz.gulimall.coupon.dao.SeckillSessionDao;
import com.xjz.gulimall.coupon.entity.SeckillSessionEntity;
import com.xjz.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    // 格式化模板
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLaste3DaysSession() {
        String startTime = getStartTime();
        String endTime = getEndTime();
        QueryWrapper<SeckillSessionEntity> queryWrapper=new QueryWrapper<>();
        //筛选出所有开始时间落在 [2026-08-08 00:00:00, 2026-08-10 23:59:59] 范围内的场次
        //场次的start_time>当日最小时间且end_time要<第三日最大时间
        queryWrapper.ge("start_time",startTime);
        queryWrapper.lt("end_time",endTime);
        //找到对应场次
        List<SeckillSessionEntity> list = this.list(queryWrapper);
        if(list==null||list.isEmpty())
        {
            return null;
        }
        //根据场次信息找到对应的商品关联信息
        return list.stream().map(new Function<SeckillSessionEntity, SeckillSessionEntity>() {
            @Override
            public SeckillSessionEntity apply(SeckillSessionEntity seckillSessionEntity) {
                Long sessionEntityId = seckillSessionEntity.getId();
                //查询出一个场次所关联的商品信息列表
                List<SeckillSkuRelationEntity> skuRelationEntities = relationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionEntityId));
                seckillSessionEntity.setSkuRelationEntityList(skuRelationEntities);
                return seckillSessionEntity;
            }
        }).collect(Collectors.toList());
    }
    /**
     * 获取今天 00:00:00 的格式化字符串
     */
    public static String getStartTime() {
        LocalDate now = LocalDate.now();
        // 拼接当天最小时间 00:00:00
        LocalDateTime startOfDay = LocalDateTime.of(now, LocalTime.MIN);
        return startOfDay.format(FORMATTER);
    }
    /**
     * 获取第 3 天 (后天) 23:59:59 的格式化字符串
     */
    public static String getEndTime() {
        LocalDate now = LocalDate.now();
        // 今天 + 2 天 = 后天
        LocalDate plusDays = now.plusDays(2);
        // 拼接当天最大时间 23:59:59 (注意：避坑 LocalTime.MAX)
        LocalDateTime endOfDay = LocalDateTime.of(plusDays, LocalTime.of(23, 59, 59));
        return endOfDay.format(FORMATTER);
    }

}