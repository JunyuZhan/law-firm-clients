package com.clientservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置静态资源服务和前端路由
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置RestTemplate Bean
     * 用于HTTP客户端调用（如回调管理系统）
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时5秒
        factory.setReadTimeout(10000); // 读取超时10秒
        return new RestTemplate(factory);
    }

    /**
     * 配置静态资源处理器
     * 服务前端构建后的静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端静态资源（构建后的dist目录）
        // 开发环境：前端通过Vite运行，不需要后端服务
        // 生产环境：前端构建后，将dist目录内容复制到src/main/resources/static/portal/目录
        registry.addResourceHandler("/portal/**")
                .addResourceLocations("classpath:/static/portal/")
                .resourceChain(false);
        
        // 根路径重定向到portal
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/portal/")
                .resourceChain(false);
    }

    /**
     * 配置视图控制器
     * 将 /portal 路径映射到前端index.html（支持前端路由）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 前端路由支持：所有/portal/*路径都返回index.html，由前端路由处理
        registry.addViewController("/portal")
                .setViewName("forward:/portal/index.html");
    }
}
