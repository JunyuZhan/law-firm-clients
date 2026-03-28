package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.SysConfigDTO;
import com.clientservice.application.service.SysConfigService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器（管理后台使用，JWT认证）
 */
@Slf4j
@Tag(name = "系统配置管理", description = "系统配置的增删改查接口（管理后台）")
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 获取配置列表
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param configKey 配置键（可选，模糊查询）
     * @param configType 配置类型（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 配置列表
     */
    @Operation(summary = "获取配置列表", description = "查询系统配置列表（管理后台使用，JWT认证）")
    @GetMapping
    public Result<List<SysConfigDTO>> getConfigList(
            @Parameter(description = "配置键") @RequestParam(required = false) final String configKey,
            @Parameter(description = "配置类型") @RequestParam(required = false) final String configType,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {

        // 获取配置列表
        List<SysConfigDTO> list = sysConfigService.getConfigList(configKey, configType, limit);

        return Result.success(list);
    }

    /**
     * 根据ID获取配置
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 配置ID
     * @return 配置DTO
     */
    @Operation(summary = "获取配置详情", description = "根据ID获取配置详情（管理后台使用，JWT认证）")
    @GetMapping("/{id}")
    public Result<SysConfigDTO> getConfigById(
            @Parameter(description = "配置ID", required = true) @PathVariable final Long id) {

        // 获取配置
        SysConfigDTO configDTO = sysConfigService.getConfigById(id);

        return Result.success(configDTO);
    }

    /**
     * 创建或更新配置
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param request 创建请求
     * @return 配置DTO
     */
    @Operation(summary = "创建或更新配置", description = "根据配置键创建或更新配置（管理后台使用，JWT认证）")
    @PostMapping
    public Result<SysConfigDTO> saveConfig(
            @RequestBody final Map<String, Object> request) {

        String configKey = (String) request.get("configKey");
        String configValue = (String) request.get("configValue");
        String configType = (String) request.get("configType");
        String description = (String) request.get("description");

        if (configKey == null || configKey.isEmpty()) {
            return Result.error("配置键不能为空");
        }

        // 创建或更新配置
        SysConfigDTO configDTO = sysConfigService.saveConfig(configKey, configValue, configType, description);

        return Result.success(configDTO);
    }

    /**
     * 更新配置
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 配置ID
     * @param request 更新请求
     * @return 配置DTO
     */
    @Operation(summary = "更新配置", description = "更新配置信息（管理后台使用，JWT认证）")
    @PutMapping("/{id}")
    public Result<SysConfigDTO> updateConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable final Long id,
            @RequestBody final Map<String, Object> request) {

        String configValue = (String) request.get("configValue");
        String configType = (String) request.get("configType");
        String description = (String) request.get("description");

        // 更新配置
        SysConfigDTO configDTO = sysConfigService.updateConfig(id, configValue, configType, description);

        return Result.success(configDTO);
    }

    /**
     * 删除配置
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 配置ID
     * @return 空结果
     */
    @Operation(summary = "删除配置", description = "删除配置（逻辑删除）（管理后台使用，JWT认证）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(
            @Parameter(description = "配置ID", required = true) @PathVariable final Long id) {

        // 删除配置
        sysConfigService.deleteConfig(id);

        return Result.success();
    }
}
