package com.xjz.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {
    @Autowired
    private SearchProperties searchProperties;
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder buider=RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS=buider.build();
    }
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        return new RestHighLevelClient(RestClient.builder(new HttpHost(searchProperties.getUrl(), searchProperties.getPort(),"http")));
    }
}
