package com.clientservice.interfaces.rest;

import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统信息控制器.
 *
 * <p>用于后台“系统信息”页面，集中提供运行时信息、关键依赖健康状态和交付所需元信息。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Tag(name = "系统信息", description = "运行时信息、依赖健康状态等系统信息接口")
public class SystemInfoController {

    private final AdminAuthorizationService adminAuthorizationService;
    private final ObjectProvider<BuildProperties> buildPropertiesProvider;
    private final ObjectProvider<HealthEndpoint> healthEndpointProvider;

    @Value("${spring.application.name:client-service}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String productVersion;

    @Value("${app.commit-sha:${APP_COMMIT_SHA:unknown}}")
    private String commitSha;

    @Value("${app.build-time:${APP_BUILD_TIME:unknown}}")
    private String buildTime;

    @Value("${app.recommended-mode:${APP_RECOMMENDED_MODE:Docker Compose}}")
    private String recommendedMode;

    @Value("${client-service.file.storage.local.path:/data/client-service/files}")
    private String fileStoragePath;

    @Operation(summary = "获取系统运行时信息", description = "返回版本、构建和部署模式等交付元信息")
    @GetMapping("/runtime-info")
    public Result<Map<String, Object>> getRuntimeInfo() {
        adminAuthorizationService.requireSuperAdmin();
        BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
        String backendVersion = buildProperties != null ? buildProperties.getVersion() : productVersion;
        String resolvedBuildTime = StringUtils.hasText(buildTime) && !"unknown".equalsIgnoreCase(buildTime)
                ? buildTime
                : buildProperties != null && buildProperties.getTime() != null
                ? buildProperties.getTime().toString()
                : "unknown";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationName", applicationName);
        result.put("productVersion", productVersion);
        result.put("backendVersion", backendVersion);
        result.put("commitSha", StringUtils.hasText(commitSha) ? commitSha : "unknown");
        result.put("buildTime", resolvedBuildTime);
        result.put("recommendedMode", recommendedMode);
        result.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return Result.success(result);
    }

    @Operation(summary = "获取关键依赖状态", description = "基于 Actuator HealthEndpoint 汇总关键依赖和本地存储状态")
    @GetMapping("/dependency-status")
    public Result<Map<String, Object>> getDependencyStatus() {
        adminAuthorizationService.requireSuperAdmin();
        HealthEndpoint healthEndpoint = healthEndpointProvider.getIfAvailable();

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, HealthComponent> components = Map.of();
        if (healthEndpoint != null) {
            components = extractComponents(healthEndpoint.health());
        }

        items.add(toDependencyItem("db", "PostgreSQL", components.get("db")));
        items.add(toDependencyItem("redis", "Redis", components.get("redis")));
        items.add(toDependencyItem("mail", "SMTP 邮件", components.get("mail")));
        items.add(toDependencyItem("diskSpace", "磁盘空间", components.get("diskSpace")));
        items.add(buildStorageItem());

        boolean hasDown = items.stream().anyMatch(item -> "DOWN".equalsIgnoreCase(String.valueOf(item.get("status")))
                || "OUT_OF_SERVICE".equalsIgnoreCase(String.valueOf(item.get("status"))));
        boolean hasUnknown = items.stream().anyMatch(item -> "UNKNOWN".equalsIgnoreCase(String.valueOf(item.get("status"))));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overallStatus", hasDown ? "DEGRADED" : hasUnknown ? "UNKNOWN" : "UP");
        result.put("items", items);
        return Result.success(result);
    }

    private Map<String, Object> buildStorageItem() {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", "storage");
        item.put("label", "文件存储");

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("path", fileStoragePath);

        try {
            Path storagePath = Path.of(fileStoragePath);
            boolean exists = Files.exists(storagePath);
            boolean writable = exists && Files.isWritable(storagePath);

            item.put("status", exists ? (writable ? "UP" : "DEGRADED") : "DOWN");
            details.put("exists", exists);
            details.put("writable", writable);
        } catch (Exception e) {
            log.warn("检查文件存储状态失败: {}", e.getMessage());
            item.put("status", "UNKNOWN");
            details.put("message", e.getMessage());
        }

        item.put("details", details);
        return item;
    }

    private Map<String, Object> toDependencyItem(String key, String label, HealthComponent component) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("label", label);

        if (component == null) {
            item.put("status", "UNKNOWN");
            item.put("details", Map.of("message", "未获取到健康检查信息"));
            return item;
        }

        Status status = component.getStatus();
        Map<String, Object> details = component instanceof Health health
                ? new LinkedHashMap<>(health.getDetails())
                : Map.of();

        item.put("status", status != null ? status.getCode() : "UNKNOWN");
        item.put("details", details.isEmpty() ? Map.of("message", "无额外信息") : details);
        return item;
    }

    private Map<String, HealthComponent> extractComponents(HealthComponent root) {
        if (root instanceof SystemHealth systemHealth) {
            return systemHealth.getComponents();
        }
        if (root instanceof CompositeHealth compositeHealth) {
            return compositeHealth.getComponents();
        }
        return Map.of();
    }
}
