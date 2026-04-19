package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.Query;
import com.xjz.gulimall.product.dao.SpuImagesDao;
import com.xjz.gulimall.product.entity.SpuImagesEntity;
import com.xjz.gulimall.product.service.SpuImagesService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long id, List<String> images) {
        if(images==null||images.size()==0)
        {
            return;
        }
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(s -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(id);
            spuImagesEntity.setImgUrl(s);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        this.saveBatch(spuImagesEntities);
    }

}