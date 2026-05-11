package com.xjz.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.search.config.ElasticSearchConfig;
import com.xjz.gulimall.search.constant.EsConstant;
import com.xjz.gulimall.search.service.ElasticsearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import to.SkuEsModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("ElasticsearchSaveService")
@Slf4j
public class ElasticsearchSaveServiceImpl implements ElasticsearchSaveService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public boolean saveUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest=new BulkRequest();
        for(SkuEsModel skuEsModel:skuEsModels)
        {
            IndexRequest indexRequest=new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s= JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        boolean b=bulk.hasFailures();
        if(b)
        {
            List<String> collect = Arrays.stream(bulk.getItems())
                    .filter(item -> item.isFailed()) // 只留下失败的项
                    .map(item -> item.getId()) // 拿到它的文档 ID (也就是 skuId)
                    .collect(Collectors.toList());
            log.error("商品上架错误（存在ES拒绝写入的项），错误商品id：{}", collect);
        }
        return b;
    }
}
