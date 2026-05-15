package com.xjz.gulimall.search.service;

import com.xjz.gulimall.search.dto.SearchParam;

import com.xjz.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
