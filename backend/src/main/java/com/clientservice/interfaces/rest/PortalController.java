package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.service.AccessLogService;
import com.clientservice.application.service.FileService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ClientMatter;
import java.util.List;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户门户控制器
 */
@Slf4j
@Tag(name = "客户门户", description = "客户访问项目详情的门户接口")
@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
public class PortalController {

    private final MatterService matterService;
    private final AccessLogService accessLogService;
    private final FileService fileService;

    /**
     * 获取项目详情（客户门户API）
     *
     * @param id 项目ID
     * @param token 访问令牌
     * @param request HTTP请求
     * @return 项目数据
     */
    @Operation(summary = "获取项目详情", description = "客户通过访问令牌查看项目详情")
    @Timed(value = "api.portal.matter.detail", description = "客户门户获取项目详情接口耗时")
    @GetMapping("/api/matter/{id}")
    public Result<ClientMatter> getMatterDetail(
            @Parameter(description = "项目ID", required = true) @PathVariable final String id,
            @Parameter(description = "访问令牌", required = true) @RequestParam(required = true) final String token,
            final HttpServletRequest request) {

        // Token 格式校验（仅允许字母、数字、短横线、下划线）
        if (token == null || token.length() < 8 || token.length() > 500
                || !token.matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new com.clientservice.common.exception.BusinessException(ErrorCode.BAD_REQUEST, "访问令牌格式无效");
        }

        // 验证令牌并获取项目
        ClientMatter matter = matterService.getMatterByToken(token);

        // 验证项目ID是否匹配
        if (!matter.getId().equals(id)) {
            throw new com.clientservice.common.exception.BusinessException(ErrorCode.FORBIDDEN, "项目ID不匹配");
        }

        // 记录访问日志
        try {
            accessLogService.recordAccess(id, matter.getClientId(), token, request);
        } catch (Exception e) {
            // 访问日志记录失败不影响主流程
            log.warn("记录访问日志失败: matterId={}", id, e);
        }

        log.info("客户访问项目详情: matterId={}, clientId={}", id, matter.getClientId());

        return Result.success(matter);
    }

    /**
     * 获取项目文件列表（客户门户API）
     * 注意：此接口与 /api/client/files 功能相同，但路径更符合门户API规范
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param status 状态（可选）
     * @return 文件列表
     */
    @Operation(summary = "获取项目文件列表", description = "客户通过访问令牌查看项目文件列表")
    @Timed(value = "api.portal.files.list", description = "客户门户获取文件列表接口耗时")
    @GetMapping("/api/files/{matterId}")
    public Result<List<ClientFileDTO>> getFiles(
            @Parameter(description = "项目ID", required = true) @PathVariable final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam(required = true) final String token,
            @Parameter(description = "状态") @RequestParam(required = false) final String status) {

        // Token 格式校验
        if (token == null || token.length() < 8 || token.length() > 500
                || !token.matches("^[a-zA-Z0-9_\\-]+$")) {
            throw new com.clientservice.common.exception.BusinessException(ErrorCode.BAD_REQUEST, "访问令牌格式无效");
        }

        // 验证令牌并获取项目
        ClientMatter matter = matterService.getMatterByToken(token);

        // 验证项目ID是否匹配
        if (!matter.getId().equals(matterId)) {
            throw new com.clientservice.common.exception.BusinessException(ErrorCode.FORBIDDEN, "项目ID不匹配");
        }

        // 获取文件列表
        List<ClientFileDTO> files = fileService.getFilesByMatterId(matterId, status);

        log.info("客户访问文件列表: matterId={}, fileCount={}", matterId, files != null ? files.size() : 0);

        return Result.success(files);
    }
}
