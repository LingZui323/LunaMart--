package com.app.mart.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置类
 *
 * @author LunaMart
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = buildCorsConfig();

        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);

        CorsFilter corsFilter = new CorsFilter(configSource);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(corsFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * 提取跨域配置为独立方法，消除"可提取方法"警告
     */
    private CorsConfiguration buildCorsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        // 放行所有原始域
        config.addAllowedOriginPattern("*");
        // 允许发送Cookie
        config.setAllowCredentials(true);
        // 放行所有请求方式
        config.addAllowedMethod("*");
        // 放行所有请求头
        config.addAllowedHeader("*");
        return config;
    }
}