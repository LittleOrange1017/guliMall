package com.xjz.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.search.constant.EsConstant;
import com.xjz.gulimall.search.dto.SearchParam;
import com.xjz.gulimall.search.service.MallSearchService;
import com.xjz.gulimall.search.vo.SearchResult;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import to.SkuEsModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchRequest searchRequest=buildSearchRequest(searchParam);
        //TODO:封装响应数据为SearchResult格式
        SearchResult result=null;
        try{
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            result=buildSearchResult(response,searchParam);
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 准备检索请求
     * 核心逻辑：将前端传来的param去构建DSL语句
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        BoolQueryBuilder booledQuery= QueryBuilders.boolQuery();
        if(!StringUtils.isEmpty(searchParam.getKeyword()))
        {
            booledQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        if(searchParam.getCatalog3Id()!=null)
        {
            booledQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        if(searchParam.getBrandId()!=null&&!searchParam.getBrandId().isEmpty())
        {
            booledQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }
        if(searchParam.getHasStock()!=null)
        {
            booledQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock()==1));
        }
        if(!StringUtils.isEmpty(searchParam.getSkuPrice()))
        {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] split = searchParam.getSkuPrice().split("-");
            if(searchParam.getSkuPrice().startsWith("-"))
            {
                rangeQuery.lte(split[1]);
            }
            else if(searchParam.getSkuPrice().endsWith("-"))
            {
                rangeQuery.gte(split[0]);
            }
            else {
                rangeQuery.gte(split[0]).lte(split[1]);
            }
            booledQuery.filter(rangeQuery);
        }
        if(searchParam.getAttrs()!=null&&!searchParam.getAttrs().isEmpty())
        {
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBool=QueryBuilders.boolQuery();
                String[] split = attr.split("_");
                String attrId=split[0];
                String[] attrValue=split[1].split(":");
                nestedBool.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBool.must(QueryBuilders.termsQuery("attrs.attrValue",attrValue));
                NestedQueryBuilder nestedQuery  = QueryBuilders.nestedQuery("attrs", nestedBool, ScoreMode.None);
                booledQuery.filter(nestedQuery);
            }
        }
        searchSourceBuilder.query(booledQuery);
        //排序
        //排序格式：sort=saleCount_asc
        if(!StringUtils.isEmpty(searchParam.getSort()))
        {
            String[] sort = searchParam.getSort().split("_");
            // 决定是升序还是降序
            SortOrder order = "asc".equalsIgnoreCase(sort[1]) ? SortOrder.ASC : SortOrder.DESC;

            // 【关键改变】：使用 SortBuilders 工厂创建 FieldSortBuilder 排序对象
            FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(sort[0]).order(order);

            // 将构建好的排序对象放入外层大底板
            searchSourceBuilder.sort(fieldSortBuilder);
        }
        //分页
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.from((searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        //高亮
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.field("skuTitle");
        highlightBuilder.preTags("<b style='color:red'>");
        highlightBuilder.postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //聚合
        //分类聚合
        TermsAggregationBuilder catalogAgg   = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);
        //品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brandImg_agg").field("brandImg").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandName_agg").field("brandName").size(1));
        searchSourceBuilder.aggregation(brandAgg);
        //属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrId_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrName_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrs.attrValue").size(30));
        attrAgg.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attrAgg);
        SearchRequest searchRequest=new SearchRequest(new String[]{"gulimall_product_new"},searchSourceBuilder );
        return searchRequest;
    }

    /**
     * 将响应结果进行拆解，组装成SearchResult
     * @param response
     * @param searchParam
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam searchParam) {
        SearchHits hits = response.getHits();
        Aggregations aggregations = response.getAggregations();
        SearchResult searchResult=new SearchResult();
        List<SkuEsModel> skuEsModels=new ArrayList<>();
        if(hits!=null&&hits.getHits().length>0)
        {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(!StringUtils.isEmpty(searchParam.getKeyword()))
                {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    if(skuTitle!=null)
                    {
                        Text fragment = skuTitle.getFragments()[0];
                        String title = fragment.string();
                        skuEsModel.setSkuTitle(title);
                    }
                }
                skuEsModels.add(skuEsModel);
            }
        }
        searchResult.setProduct(skuEsModels);
        if (hits != null) {
            if (hits.getTotalHits() != null) {
                searchResult.setTotal(hits.getTotalHits().value);
            }
        }
        searchResult.setPageNum(searchParam.getPageNum());
        int totalPages = (int) Math.ceil((double) hits.getTotalHits().value / EsConstant.PRODUCT_PAGESIZE);
        searchResult.setTotalPages(totalPages);
        //解析分类聚合
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVO> catalogVOS=new ArrayList<>();
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVO catalogVO=new SearchResult.CatalogVO();
            long catId = bucket.getKeyAsNumber().longValue();
            catalogVO.setCatalogId(catId);
            //打开子桶：获取分类名 (按分类名分组，是 String 类型，所以用 ParsedStringTerms)
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVO.setCatalogName(catalogName);
            catalogVOS.add(catalogVO);
        }
        searchResult.setCatalogs(catalogVOS);
        //解析品牌聚合
        ParsedLongTerms  brandAgg = aggregations.get("brand_agg");
        List<SearchResult.BrandVO> brandVOS=new ArrayList<>();
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVO brandVO=new SearchResult.BrandVO();
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVO.setBrandId(brandId);
            ParsedStringTerms  brandImgAgg = bucket.getAggregations().get("brandImg_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandImg(brandImg);
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandName_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVO.setBrandName(brandName);
            brandVOS.add(brandVO);
        }
        searchResult.setBrands(brandVOS);
        //解析属性聚合
        //因为在构建时最外层套了个 nested，所以第一步先用 ParsedNested 接住！
        // 获取所有已经选中的属性 ID 列表 ...
        List<Long> attrIds = new ArrayList<>();
        if(searchParam.getAttrs() != null){
            attrIds = searchParam.getAttrs().stream().map(attr -> {
                return Long.parseLong(attr.split("_")[0]);
            }).collect(Collectors.toList());
        }
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms  attrIdAgg = attrAgg.getAggregations().get("attrId_agg");
        List<SearchResult.AttrVO> attrVOS=new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVO attrVO=new SearchResult.AttrVO();
            long attrId = bucket.getKeyAsNumber().longValue();
            if(attrIds.contains(attrId))
            {
                continue;
            }
            attrVO.setAttrId(attrId);
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrName_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVO.setAttrName(attrName);
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValue_agg");
            List<String> attrVales = attrValueAgg.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString)
                    .flatMap(str-> Arrays.stream(str.split(";")))
                    .distinct()
                    .collect(Collectors.toList());
            attrVO.setAttrValue(attrVales);
            attrVOS.add(attrVO);
        }
        searchResult.setAttrs(attrVOS);
        if(searchParam.getAttrs()!=null&&!searchParam.getAttrs().isEmpty())
        {
            List<SearchResult.NavVo> collect = searchParam.getAttrs().stream().map(new Function<String, SearchResult.NavVo>() {
                @Override
                public SearchResult.NavVo apply(String attr) {
                    SearchResult.NavVo navVo = new SearchResult.NavVo();
                    String[] split = attr.split("_");
                    navVo.setNavValue(split[1]);
                    navVo.setNavName("属性");
                    String oldQueryString = searchParam.getOldQueryString();
                    String encode= URLEncoder.DEFAULT.encode(attr,StandardCharsets.UTF_8);
                    String replace = oldQueryString.replace("&attrs=" + encode, "");
                    navVo.setLink("http://search.littleorange.com/list.html?" + replace);
                    return navVo;
                }
            }).collect(Collectors.toList());
            searchResult.setNavs(collect);
        }
        return searchResult;
    }
}
