package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ApiKeyCreateRequest;
import com.clientservice.application.dto.ApiKeyDTO;
import com.clientservice.application.dto.ApiKeyUpdateRequest;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

/**
 * API密钥管理控制器（管理后台使用，JWT认证）
 */
@Slf4j
@Tag(name = "API密钥管理", description = "API密钥的增删改查接口（管理后台）")
@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AdminAuthorizationService adminAuthorizationService;

    /**
     * 获取API密钥列表
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param enabled 是否启用（可选）
     * @param lawFirmName 律所名称（可选，模糊查询）
     * @param limit 限制数量（可选，默认100）
     * @return API密钥列表
     */
    @Operation(summary = "获取API密钥列表", description = "查询API密钥列表（管理后台使用，JWT认证）")
    @GetMapping
    public Result<List<ApiKeyDTO>> getApiKeyList(
            @Parameter(description = "是否启用") @RequestParam(required = false) final Boolean enabled,
            @Parameter(description = "律所名称") @RequestParam(required = false) final String lawFirmName,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {
        adminAuthorizationService.requireSuperAdmin();

        // 获取API密钥列表
        List<ApiKeyDTO> list = apiKeyService.getApiKeyList(enabled, lawFirmName, limit);

        return Result.success(list);
    }

    /**
     * 根据ID获取API密钥
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id API密钥ID
     * @return API密钥DTO
     */
    @Operation(summary = "获取API密钥详情", description = "根据ID获取API密钥详情（管理后台使用，JWT认证）")
    @GetMapping("/{id}")
    public Result<ApiKeyDTO> getApiKeyById(
            @Parameter(description = "API密钥ID", required = true) @PathVariable final Long id) {
        adminAuthorizationService.requireSuperAdmin();

        // 获取API密钥
        ApiKeyDTO apiKeyDTO = apiKeyService.getApiKeyById(id);

        return Result.success(apiKeyDTO);
    }

    /**
     * 创建API密钥
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param request 创建请求
     * @return API密钥DTO
     */
    @Operation(summary = "创建API密钥", description = "创建新的API密钥（管理后台使用，JWT认证）")
    @PostMapping
    public Result<ApiKeyDTO> createApiKey(
            @Valid @RequestBody final ApiKeyCreateRequest request) {
        adminAuthorizationService.requireSuperAdmin();

        // 创建API密钥（lawFirmName 为可选字段）
        ApiKeyDTO apiKeyDTO = apiKeyService.createApiKey(
                request.getKeyName(),
                request.getLawFirmName(),
                request.getExpiresAt()
        );

        return Result.success(apiKeyDTO);
    }

    /**
     * 更新API密钥
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id API密钥ID
     * @param request 更新请求
     * @return API密钥DTO
     */
    @Operation(summary = "更新API密钥", description = "更新API密钥信息（管理后台使用，JWT认证）")
    @PutMapping("/{id}")
    public Result<ApiKeyDTO> updateApiKey(
            @Parameter(description = "API密钥ID", required = true) @PathVariable final Long id,
            @Valid @RequestBody final ApiKeyUpdateRequest request) {
        adminAuthorizationService.requireSuperAdmin();

        // 更新API密钥
        ApiKeyDTO apiKeyDTO = apiKeyService.updateApiKey(
                id,
                request.getKeyName(),
                request.getLawFirmName(),
                request.getEnabled(),
                request.getExpiresAt()
        );

        return Result.success(apiKeyDTO);
    }

    /**
     * 删除API密钥
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id API密钥ID
     * @return 空结果
     */
    @Operation(summary = "删除API密钥", description = "删除API密钥（逻辑删除）（管理后台使用，JWT认证）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiKey(
            @Parameter(description = "API密钥ID", required = true) @PathVariable final Long id) {
        adminAuthorizationService.requireSuperAdmin();

        // 删除API密钥
        apiKeyService.deleteApiKey(id);

        return Result.success();
    }
}
