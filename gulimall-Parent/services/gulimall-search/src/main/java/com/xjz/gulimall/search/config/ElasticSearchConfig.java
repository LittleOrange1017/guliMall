package com.xjz.gulimall.search.config;

import org.elasticsearch.client.RequestOptions;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    private static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder buider=RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS=buider.build();
    }
}
