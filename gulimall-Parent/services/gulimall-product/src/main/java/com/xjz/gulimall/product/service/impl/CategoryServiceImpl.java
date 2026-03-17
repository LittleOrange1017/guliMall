package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import utils.Query;
import com.xjz.gulimall.product.dao.CategoryDao;
import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;

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
        List<CategoryEntity> levelsOne = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid()==0).
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
                        return categoryEntity.getParentCid()==root.getCatId();
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
}