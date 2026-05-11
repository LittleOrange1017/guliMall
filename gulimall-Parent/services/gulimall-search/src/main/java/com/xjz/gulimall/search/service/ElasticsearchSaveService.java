package com.xjz.gulimall.search.service;

import org.springframework.stereotype.Service;
import to.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ElasticsearchSaveService {
    boolean saveUp(List<SkuEsModel> skuEsModels) throws IOException;
}
