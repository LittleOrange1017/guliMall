package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dto.Attr;
import com.xjz.gulimall.product.dto.SpuSaveDto;
import com.xjz.gulimall.product.entity.*;
import com.xjz.gulimall.product.feign.CouponFeignClient;
import com.xjz.gulimall.product.feign.SearchFeignClient;
import com.xjz.gulimall.product.feign.WareFeignClient;
import com.xjz.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import to.SkuEsModel;
import to.SkuReductionTo;
import to.SkuStockTo;
import to.SpuBoundTo;
import utils.Query;
import com.xjz.gulimall.product.dao.SpuInfoDao;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.R;
import vo.SkuHasStockVo;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Transactional
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private CouponFeignClient couponFeginClient;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WareFeignClient wareFeignClient;
    @Autowired
    private AttrService attrService;
    @Autowired
    private SearchFeignClient searchFeignClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpu(SpuSaveDto dto) {
        //1.保存spu信息
        SpuInfoEntity spuInfoEntity=new SpuInfoEntity();
        BeanUtils.copyProperties(dto,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        //2.保存spu的描述图集
        List<String> decript = dto.getDecript();
        SpuInfoDescEntity spuInfoDescEntity=new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        //我们将图片数组转为逗号分割的字符串
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        //3.保存spu的图片集
        List<String> images = dto.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.保存spu的规格参数
        List<SpuSaveDto.BaseAttrs> baseAttrs = dto.getBaseAttrs();
        productAttrValueService.saveProductAttrValue(spuInfoEntity.getId(),baseAttrs);
        //5.保存spu的积分信息
        SpuBoundTo spuBoundTo=new SpuBoundTo();
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        spuBoundTo.setBuyBounds(dto.getBounds().buyBounds);
        spuBoundTo.setGrowBounds(dto.getBounds().growBounds);
        R r = couponFeginClient.saveSpuBounds(spuBoundTo);
        if(!r.get("code").equals(0))
        {
            throw new RuntimeException("保存积分信息失败");
        }
        //6.保存SKU
        List<SpuSaveDto.Skus> skus = dto.getSkus();
        if(skus!=null&&skus.size()>0)
        {
            skus.forEach(skus1 -> saveSkuInfo(skus1, spuInfoEntity));
        }
    }

    @Override
    public void saveSkuInfo(SpuSaveDto.Skus skus, SpuInfoEntity spuInfoEntity) {
        //1.1 保存SKU基本信息
        SkuInfoEntity skuInfoEntity=new SkuInfoEntity();
        BeanUtils.copyProperties(skus,skuInfoEntity);
        skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
        skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
        skuInfoEntity.setSpuId(spuInfoEntity.getId());
        String desc=new String();
        for (String s : skus.getDescar()) {
            desc.join(",",s);
        }
        skuInfoEntity.setSkuDesc(desc);
        //1.2.保存SKU的默认图片信息
        String defaultImg="";
        List<SpuSaveDto.Images> images = skus.getImages();
        for (SpuSaveDto.Images image : images) {
            if(image.getDefaultImg()==1)
            {
                defaultImg=image.getImgUrl();
            }
        }
        skuInfoEntity.setSkuDefaultImg(defaultImg);
        skuInfoService.saveSkuInfo(skuInfoEntity);
        //1.3.拿到生成的自增ID
        Long skuId = skuInfoEntity.getSkuId();
        //2 保存SKU的图片信息
        List<SkuImagesEntity> imagesEntities = skus.getImages().stream()
                .filter(img -> StringUtils.hasLength(img.getImgUrl())) // 过滤空图
                .map(img -> {
                    SkuImagesEntity entity = new SkuImagesEntity();
                    entity.setSkuId(skuId);
                    entity.setImgUrl(img.getImgUrl());
                    entity.setDefaultImg(img.getDefaultImg());
                    return entity;
                }).collect(Collectors.toList());
        skuImagesService.saveImages(imagesEntities);
        //3 保存SKU的销售属性
        List<Attr> attr = skus.getAttr();
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(new Function<Attr, SkuSaleAttrValueEntity>() {
            @Override
            public SkuSaleAttrValueEntity apply(Attr attr) {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }
        }).collect(Collectors.toList());
        skuSaleAttrValueService.saveSkuSaleAttrValue(skuSaleAttrValueEntities);
        //4 远程调用 保存SKU的优惠信息
        SkuReductionTo skuReductionTo=new SkuReductionTo();
        BeanUtils.copyProperties(skus,skuReductionTo);
        skuReductionTo.setSkuId(skuId);
        if (skus.getMemberPrice() != null && !skus.getMemberPrice().isEmpty()) {
            List<SkuReductionTo.MemberPrice> memberPrices = skus.getMemberPrice().stream().map(item -> {
                SkuReductionTo.MemberPrice memberPrice = new SkuReductionTo.MemberPrice();
                memberPrice.setId((long) item.getId());
                memberPrice.setName(item.getName());
                memberPrice.setPrice(item.getPrice());
                return memberPrice;
            }).collect(Collectors.toList());
            skuReductionTo.setMemberPrice(memberPrices);
        }
        boolean hasCountDiscount = skuReductionTo.getFullCount() > 0;
        boolean hasPriceReduction = skuReductionTo.getFullPrice() != null
                && skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0;
        boolean hasMemberPrice = skuReductionTo.getMemberPrice() != null
                && !skuReductionTo.getMemberPrice().isEmpty();
        if (hasCountDiscount || hasPriceReduction || hasMemberPrice) {
            R r = couponFeginClient.saveSkuReduction(skuReductionTo,skus.getPrice());
            if (!r.get("code").equals(0)) {
                throw new RuntimeException("保存优惠信息失败");
            }
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper=new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!org.apache.commons.lang.StringUtils.isEmpty(key))
        {
            queryWrapper.and(spuInfoEntityQueryWrapper -> spuInfoEntityQueryWrapper.eq("id",key).or().like("spu_name",key));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void up(Long spuId) {
        //TODO 1、根据spuId查询出对应的sku列表
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkuBySpuId(spuId);
        //TODO 2、查询出SkuId的集合
        List<Long> skuIdList = skuInfoEntities.stream().map(skuInfoEntity -> skuInfoEntity.getSkuId()).collect(Collectors.toList());
        //TODO 3、查出当前 spu 的所有基础规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        //TODO 4、拿着attrIds集合，去 pms_attr 表里过滤出 search_type = 1 的属性Id
        List<Long> attrIds = baseAttrs.stream().map(productAttrValueEntity -> productAttrValueEntity.getAttrId()).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        //TODO 5、组装最终需要放入 ES 的属性集合
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(productAttrValueEntity -> idSet.contains(productAttrValueEntity.getAttrId()))
                .map(productAttrValueEntity -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    attrs.setAttrId(productAttrValueEntity.getAttrId());
                    attrs.setAttrValue(productAttrValueEntity.getAttrValue());
                    attrs.setAttrName(productAttrValueEntity.getAttrName());
                    return attrs;
                }).collect(Collectors.toList());
        //TODO 6、发送库存系统远程调用，批量查询对应的SkuId的库存是否有无
        Map<Long, Boolean> stockMap = null;
        try {
            SkuStockTo skuStockTo = new SkuStockTo();
            skuStockTo.setSkuId(skuIdList);
            List<SkuHasStockVo> hasStockList = wareFeignClient.getSkuStockBySpuId(skuStockTo);
            // 核心魔法：将 List 转化为 Map<skuId, hasStock>
            if (hasStockList != null) {
                stockMap = hasStockList.stream().collect(
                        Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock)
                );
            }
        } catch (Exception e) {
            log.error("库存服务调用异常：原因 {}", e);
            // 如果触发了 catch（比如网络中断），stockMap 将保持为 null。
            // 这是一种防御性编程策略：如果查库存失败，后续代码我们会默认设定为有库存（容错兜底）。
        }
        // 优化：品牌和分类信息同一个 SPU 都是一样的，查1次就行！
        BrandEntity brand = brandService.getById(skuInfoEntities.get(0).getBrandId());
        CategoryEntity category = categoryService.getById(skuInfoEntities.get(0).getCatalogId());
        //TODO 7、正式组装 ES 数据模型
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(new Function<SkuInfoEntity, SkuEsModel>() {
            @Override
            public SkuEsModel apply(SkuInfoEntity skuInfoEntity) {
                SkuEsModel skuEsModel = new SkuEsModel();
                BeanUtils.copyProperties(skuInfoEntity, skuEsModel);
                skuEsModel.setHotScore(0L);
                skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());
                skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
                skuEsModel.setBrandName(brand.getName());
                skuEsModel.setBrandImg(brand.getLogo());
                skuEsModel.setCatalogName(category.getName());
                // 挂载循环外组装好的检索属性
                skuEsModel.setAttrs(attrsList);
                skuEsModel.setHasStock(finalStockMap.get(skuEsModel.getSkuId()));
                return skuEsModel;
            }
        }).collect(Collectors.toList());
        //TODO 8、批量发给 ES 保存
        R r = searchFeignClient.saveUp(skuEsModels);
        if(r.get("code").equals(0))
        {
            // 远程调用成功，修改spu状态为"已上架"
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setPublishStatus(1);
            spuInfoEntity.setUpdateTime(new Date());
            this.updateById(spuInfoEntity);
        }
    }
}
