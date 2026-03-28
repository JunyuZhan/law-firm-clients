package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.NotificationTemplateCreateRequest;
import com.clientservice.application.dto.NotificationTemplateDTO;
import com.clientservice.application.dto.NotificationTemplateUpdateRequest;
import com.clientservice.application.service.NotificationTemplateService;
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
 * 通知模板管理控制器（管理后台使用，JWT认证）
 */
@Slf4j
@Tag(name = "通知模板管理", description = "短信和微信模板的增删改查接口（管理后台）")
@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    /**
     * 获取模板列表
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param templateType 模板类型（可选）
     * @param provider 提供商（可选）
     * @param enabled 是否启用（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 模板列表
     */
    @Operation(summary = "获取模板列表", description = "查询通知模板列表（管理后台使用，JWT认证）")
    @GetMapping
    public Result<List<NotificationTemplateDTO>> getTemplateList(
            @Parameter(description = "模板类型") @RequestParam(required = false) final String templateType,
            @Parameter(description = "提供商") @RequestParam(required = false) final String provider,
            @Parameter(description = "是否启用") @RequestParam(required = false) final Boolean enabled,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {

        // 获取模板列表
        List<NotificationTemplateDTO> list = templateService.getTemplateList(templateType, provider, enabled, limit);

        return Result.success(list);
    }

    /**
     * 根据ID获取模板
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 模板ID
     * @return 模板DTO
     */
    @Operation(summary = "获取模板详情", description = "根据ID获取模板详情（管理后台使用，JWT认证）")
    @GetMapping("/{id}")
    public Result<NotificationTemplateDTO> getTemplateById(
            @Parameter(description = "模板ID", required = true) @PathVariable final Long id) {

        // 获取模板
        NotificationTemplateDTO templateDTO = templateService.getTemplateById(id);

        return Result.success(templateDTO);
    }

    /**
     * 创建模板
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param request 创建请求
     * @return 模板DTO
     */
    @Operation(summary = "创建模板", description = "创建新的通知模板（管理后台使用，JWT认证）")
    @PostMapping
    public Result<NotificationTemplateDTO> createTemplate(
            @Valid @RequestBody final NotificationTemplateCreateRequest request) {

        // 创建模板
        NotificationTemplateDTO templateDTO = templateService.createTemplate(
                request.getTemplateName(),
                request.getTemplateType(),
                request.getTemplateCode(),
                request.getTemplateContent(),
                request.getTemplateVariables(),
                request.getProvider(),
                request.getSignName(),
                request.getEnabled(),
                request.getDescription());

        return Result.success(templateDTO);
    }

    /**
     * 更新模板
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 模板ID
     * @param request 更新请求
     * @return 模板DTO
     */
    @Operation(summary = "更新模板", description = "更新模板信息（管理后台使用，JWT认证）")
    @PutMapping("/{id}")
    public Result<NotificationTemplateDTO> updateTemplate(
            @Parameter(description = "模板ID", required = true) @PathVariable final Long id,
            @Valid @RequestBody final NotificationTemplateUpdateRequest request) {

        // 更新模板
        NotificationTemplateDTO templateDTO = templateService.updateTemplate(
                id,
                request.getTemplateName(),
                request.getTemplateContent(),
                request.getTemplateVariables(),
                request.getEnabled(),
                request.getDescription());

        return Result.success(templateDTO);
    }

    /**
     * 删除模板
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 模板ID
     * @return 空结果
     */
    @Operation(summary = "删除模板", description = "删除模板（逻辑删除）（管理后台使用，JWT认证）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(
            @Parameter(description = "模板ID", required = true) @PathVariable final Long id) {

        // 删除模板
        templateService.deleteTemplate(id);

        return Result.success();
    }
}
