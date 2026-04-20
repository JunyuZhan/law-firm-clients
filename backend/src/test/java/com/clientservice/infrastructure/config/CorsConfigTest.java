package com.clientservice.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.Filter;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@DisplayName("CorsConfig 单元测试")
class CorsConfigTest {

    @Test
    @DisplayName("生产环境未配置白名单时不应允许任意来源")
    void corsFilter_ShouldNotAllowAnyOriginInProduction() throws Exception {
        CorsConfig config = new CorsConfig(environmentWithProfiles());
        setField(config, "allowedOrigins", "");
        setField(config, "allowedMethods", "GET,POST");
        setField(config, "allowedHeaders", "Authorization,Content-Type");

        CorsConfiguration cors = extractCorsConfiguration(config.corsFilter());

        assertEquals(null, cors.getAllowedOrigins());
        assertEquals(null, cors.getAllowedOriginPatterns());
        assertTrue(Boolean.TRUE.equals(cors.getAllowCredentials()));
    }

    @Test
    @DisplayName("开发环境未配置白名单时仅允许localhost")
    void corsFilter_ShouldAllowOnlyLocalOriginsInDev() throws Exception {
        CorsConfig config = new CorsConfig(environmentWithProfiles("dev"));
        setField(config, "allowedOrigins", "");
        setField(config, "allowedMethods", "GET,POST");
        setField(config, "allowedHeaders", "Authorization,Content-Type");

        CorsConfiguration cors = extractCorsConfiguration(config.corsFilter());

        assertTrue(cors.getAllowedOriginPatterns().contains("http://localhost:*"));
        assertFalse(cors.getAllowedOriginPatterns().contains("*"));
    }

    @Test
    @DisplayName("显式配置白名单时应使用配置值")
    void corsFilter_ShouldUseConfiguredOrigins() throws Exception {
        CorsConfig config = new CorsConfig(environmentWithProfiles());
        setField(config, "allowedOrigins", "https://admin.example.com, https://portal.example.com");
        setField(config, "allowedMethods", "GET,POST");
        setField(config, "allowedHeaders", "Authorization,Content-Type");

        CorsConfiguration cors = extractCorsConfiguration(config.corsFilter());

        assertEquals(List.of("https://admin.example.com", "https://portal.example.com"), cors.getAllowedOrigins());
    }

    private Environment environmentWithProfiles(String... profiles) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        return environment;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private CorsConfiguration extractCorsConfiguration(FilterRegistrationBean<CorsFilter> bean) {
        Filter filter = bean.getFilter();
        CorsFilter corsFilter = (CorsFilter) filter;
        CorsConfigurationSource source = (CorsConfigurationSource) org.springframework.test.util.ReflectionTestUtils
                .getField(corsFilter, "configSource");
        return source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/admin/auth/me"));
    }
}
