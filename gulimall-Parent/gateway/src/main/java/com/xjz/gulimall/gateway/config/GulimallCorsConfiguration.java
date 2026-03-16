package com.xjz.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: GulimallCorsConfiguration
 * Package:com.xjz.gulimall.gateway.config
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/16 14:29
 * @Version 1.0
 */
@Configuration
public class GulimallCorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource=new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration=new CorsConfiguration();
        configuration.setAllowCredentials(true);
        List<String> allowedHeaders=new ArrayList<>();
        allowedHeaders.add("*");
        configuration.setAllowedHeaders(allowedHeaders);
        List<String> allowedMethods=new ArrayList<>();
        allowedMethods.add("*");
        configuration.setAllowedMethods(allowedMethods);
        List<String> allowedOrigins=new ArrayList<>();
        allowedOrigins.add("*");
        configuration.setAllowedOriginPatterns(allowedOrigins);
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",configuration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
