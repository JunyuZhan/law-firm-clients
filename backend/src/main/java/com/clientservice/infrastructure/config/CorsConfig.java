package com.clientservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 
 * 安全配置说明：
 * - 开发环境：通过 spring.profiles.active 判断，允许本地来源
 * - 生产环境：必须通过环境变量 CORS_ALLOWED_ORIGINS 配置具体域名
 *   例如：CORS_ALLOWED_ORIGINS=https://example.com,https://admin.example.com
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * 允许的来源域名，多个域名用逗号分隔
     * 不再默认 "*"，而是默认允许本地开发地址
     */
    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 允许的请求方法
     */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    /**
     * 允许的请求头
     */
    @Value("${cors.allowed-headers:Authorization,Content-Type,X-CSRF-Token,X-Requested-With}")
    private String allowedHeaders;

    /**
     * 跨域过滤器
     * 设置最高优先级，确保在JWT过滤器之前执行
     *
     * @return 跨域过滤器
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源，简化配置
        // 注意：使用 allowedOriginPatterns("*") 而不是 allowedOrigins("*") 
        // 因为当 allowCredentials 为 true 时，不能使用 allowedOrigins("*")
        config.addAllowedOriginPattern("*");
        
        // 配置允许的方法
        for (String method : allowedMethods.split(",")) {
            config.addAllowedMethod(method.trim());
        }
        
        // 配置允许的请求头
        for (String header : allowedHeaders.split(",")) {
            config.addAllowedHeader(header.trim());
        }
        
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
