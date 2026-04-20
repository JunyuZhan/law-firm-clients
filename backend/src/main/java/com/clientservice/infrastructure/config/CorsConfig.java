package com.clientservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
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

    private final Environment environment;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

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

        configureAllowedOrigins(config);
        
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

    private void configureAllowedOrigins(CorsConfiguration config) {
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    config.addAllowedOrigin(trimmed);
                }
            }
            return;
        }

        if (isLocalProfile()) {
            config.addAllowedOriginPattern("http://localhost:*");
            config.addAllowedOriginPattern("http://127.0.0.1:*");
            config.addAllowedOriginPattern("http://[::1]:*");
            config.addAllowedOriginPattern("https://localhost:*");
            log.info("CORS 未配置白名单，当前为本地/测试环境，仅允许 localhost 来源");
            return;
        }

        log.warn("CORS 未配置允许来源，生产/非本地环境将拒绝所有跨域浏览器请求");
    }

    private boolean isLocalProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile)
                    || "local".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
