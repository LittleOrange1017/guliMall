package com.xjz.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.service.CategoryBrandRelationService;
import com.xjz.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.C;
import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import utils.Query;
import com.xjz.gulimall.product.dao.CategoryDao;
import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> entities = categoryDao.selectList(null);
        // 2.组装成父子的树形结构
        List<CategoryEntity> levelsOne = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(0L)).
                map(new Function<CategoryEntity, CategoryEntity>() {
                    @Override
                    public CategoryEntity apply(CategoryEntity item) {
                        item.setChildren(getChildren(item, entities));
                        return item;
                    }
                }).sorted(new Comparator<CategoryEntity>() {
                    @Override
                    public int compare(CategoryEntity o1, CategoryEntity o2) {
                        return (o1.getSort() == null ? 0 : o1.getSort()) - (o2.getSort() == null ? 0 : o2.getSort());
                    }
                }).collect(Collectors.toList());
        return levelsOne;
    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> chidren = list.stream()
                .filter(new Predicate<CategoryEntity>() {
                    @Override
                    public boolean test(CategoryEntity categoryEntity) {
                        return categoryEntity.getParentCid().equals(root.getCatId());
                    }
                })
                .map(new Function<CategoryEntity, CategoryEntity>() {
                    @Override
                    public CategoryEntity apply(CategoryEntity item) {
                        item.setChildren(getChildren(item, list));
                        return item;
                    }
                })
                .sorted(new Comparator<CategoryEntity>() {
                    @Override
                    public int compare(CategoryEntity o1, CategoryEntity o2) {
                        return (o1.getSort() == null ? 0 : o1.getSort()) - (o2.getSort() == null ? 0 : o2.getSort());
                    }
                })
                .collect(Collectors.toList());
        return chidren;
    }
    @Override
    public int removeCategoryByIds(List<Long> asList) {
        //Todo 删除之前需要判断当前删除的菜单，是否被其他地方引用
        return baseMapper.deleteBatchIds(asList);
    }

    @Override
    public int saveCategory(CategoryEntity category) {
        return categoryDao.insert(category);
    }

    @Override
    @Transactional
    public int updateCategoryById(CategoryEntity category) {
        UpdateWrapper<CategoryEntity> updateWrapper = new UpdateWrapper<CategoryEntity>();
        updateWrapper.eq("cat_id", category.getCatId());
        if(!StringUtils.isEmpty(category.getName()))
        {
            categoryBrandRelationService.updateCategoryName(category.getCatId(), category.getName());
        }
        return categoryDao.update(category, updateWrapper);
    }

    @Override
    public Long[] findCatelogIds(Long catelogId) {
        CategoryEntity entity = categoryDao.selectById(catelogId);
        List<Long> catelogIds = new ArrayList<>();
        findParentPath(catelogId,catelogIds);
        Collections.reverse(catelogIds);
        return catelogIds.toArray(new Long[0]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        QueryWrapper<CategoryEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("cat_level",1);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        String catalogJSONKey = "catalogJSON";
        String catelogJSON = redisTemplate.opsForValue().get(catalogJSONKey);
        if(StringUtils.isEmpty(catelogJSON))
        {
            Map<String, List<Catelog2Vo>> stringListMap = getStringListMap();
            String json = JSON.toJSONString(stringListMap);
            redisTemplate.opsForValue().set(catalogJSONKey,json,500, TimeUnit.SECONDS);
            return stringListMap;
        }
        else
        {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(
                    catelogJSON,
                    new TypeReference<Map<String, List<Catelog2Vo>>>() {}
            );
            return result;
        }
    }

    private @NonNull Map<String, List<Catelog2Vo>> getStringListMap() {
        List<CategoryEntity> categoryEntities = this.listWithTree();
        Map<String, List<Catelog2Vo>> map = new HashMap<>();
        for(CategoryEntity level1:categoryEntities)
        {
            String catelog1Id = level1.getCatId().toString();
            List<Catelog2Vo> catelog2Vos=new ArrayList<>();
            List<CategoryEntity> level2Entites = level1.getChildren();
            if(level2Entites!=null&&!level2Entites.isEmpty())
            {
                for(CategoryEntity level2:level2Entites)
                {
                    Catelog2Vo catelog2Vo=new Catelog2Vo();
                    catelog2Vo.setCatelog1Id(catelog1Id);
                    catelog2Vo.setId(level2.getCatId().toString());
                    catelog2Vo.setName(level2.getName());
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos=new ArrayList<>();
                    List<CategoryEntity> level3Entites = level2.getChildren();
                    if(level3Entites!=null&&!level3Entites.isEmpty())
                    {
                        for(CategoryEntity level3:level3Entites)
                        {
                            Catelog2Vo.Catelog3Vo catelog3Vo=new Catelog2Vo.Catelog3Vo();
                            catelog3Vo.setCatelog2Id(catelog2Vo.getId());
                            catelog3Vo.setName(level3.getName());
                            catelog3Vo.setId(level3.getCatId().toString());
                            catelog3Vos.add(catelog3Vo);
                        }
                    }
                    catelog2Vo.setCatelog3VoList(catelog3Vos);
                    catelog2Vos.add(catelog2Vo);
                }
            }
            map.put(catelog1Id,catelog2Vos);
        }
        return map;
    }

    private void findParentPath(Long catelogId, List<Long> catelogIds) {
        catelogIds.add(catelogId);
        CategoryEntity entity = categoryDao.selectById(catelogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), catelogIds);
        }
    }
}