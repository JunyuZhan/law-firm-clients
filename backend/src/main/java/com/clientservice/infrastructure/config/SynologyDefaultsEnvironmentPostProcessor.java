package com.clientservice.infrastructure.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringUtils;

/**
 * 为群晖零交互部署提供基础设施默认值。
 */
public class SynologyDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "synologyInfraDefaults";
    private static final String DEFAULT_DATASOURCE_URL =
            "jdbc:postgresql://archive-postgres:5432/client_service";
    private static final String DEFAULT_REDIS_HOST = "archive-redis";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return;
        }

        Map<String, Object> defaults = new LinkedHashMap<>();

        putIfMissing(environment, defaults, "spring.datasource.url", DEFAULT_DATASOURCE_URL);
        putIfMissing(environment, defaults, "spring.datasource.username", "postgres");
        putIfMissing(environment, defaults, "spring.datasource.password", "");

        String redisHost = firstNonBlank(
                environment.getProperty("SPRING_DATA_REDIS_HOST"),
                environment.getProperty("SPRING_REDIS_HOST"),
                DEFAULT_REDIS_HOST);
        String redisPort = firstNonBlank(
                environment.getProperty("SPRING_DATA_REDIS_PORT"),
                environment.getProperty("SPRING_REDIS_PORT"),
                "6379");
        String redisPassword = firstNonBlank(
                environment.getProperty("SPRING_DATA_REDIS_PASSWORD"),
                environment.getProperty("SPRING_REDIS_PASSWORD"),
                "");
        String redisDatabase = firstNonBlank(
                environment.getProperty("SPRING_DATA_REDIS_DATABASE"),
                environment.getProperty("SPRING_REDIS_DATABASE"),
                "0");

        putIfMissing(environment, defaults, "spring.data.redis.host", redisHost);
        putIfMissing(environment, defaults, "spring.data.redis.port", redisPort);
        putIfMissing(environment, defaults, "spring.data.redis.password", redisPassword);
        putIfMissing(environment, defaults, "spring.data.redis.database", redisDatabase);
        putIfMissing(environment, defaults, "client-service.system.base-url", DEFAULT_BASE_URL);

        if (!defaults.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
        }
    }

    private void putIfMissing(
            ConfigurableEnvironment environment,
            Map<String, Object> defaults,
            String key,
            String value) {
        if (!StringUtils.hasText(environment.getProperty(key))) {
            defaults.put(key, value);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
