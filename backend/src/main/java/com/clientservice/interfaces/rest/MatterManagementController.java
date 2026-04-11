package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.MatterListDTO;
import com.clientservice.application.dto.MatterDetailDTO;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目管理控制器（管理后台使用，JWT认证）
 */
@Slf4j
@Tag(name = "项目管理", description = "项目访问管理接口（管理后台）")
@RestController
@RequestMapping("/api/matter")
@RequiredArgsConstructor
public class MatterManagementController {

    private final MatterService matterService;
    private final AdminAuthorizationService adminAuthorizationService;

    /**
     * 撤销项目访问
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param matterId 项目ID
     * @return 空结果
     */
    @Operation(summary = "撤销项目访问", description = "撤销项目的访问权限，使访问链接失效（管理后台使用，JWT认证）")
    @PostMapping("/revoke")
    public Result<Void> revokeMatter(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId) {
        adminAuthorizationService.requireSuperAdmin();

        // 撤销项目访问
        matterService.revokeMatter(matterId);

        log.info("项目访问已撤销: matterId={}", matterId);

        return Result.success();
    }

    /**
     * 获取项目列表
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param clientId 客户ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 项目列表
     */
    @Operation(summary = "获取项目列表", description = "查询项目列表（管理后台使用，JWT认证）")
    @GetMapping("/list")
    public Result<List<MatterListDTO>> getMatterList(
            @Parameter(description = "客户ID") @RequestParam(required = false) final Long clientId,
            @Parameter(description = "状态") @RequestParam(required = false) final String status,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
                final LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
                final LocalDateTime endTime,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {
        adminAuthorizationService.requireSuperAdmin();

        // 获取项目列表
        List<MatterListDTO> list = matterService.getMatterList(clientId, status, startTime, endTime, limit);

        return Result.success(list);
    }

    /**
     * 获取项目详情（管理后台）
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param id 项目ID
     * @return 项目详情
     */
    @Operation(summary = "获取项目详情", description = "根据ID获取项目详情（管理后台使用，JWT认证）")
    @GetMapping("/detail/{id}")
    public Result<MatterDetailDTO> getMatterDetail(
            @Parameter(description = "项目ID", required = true) @PathVariable final String id) {
        adminAuthorizationService.requireSuperAdmin();

        // 获取项目详情
        MatterDetailDTO detail = matterService.getMatterDetail(id);

        return Result.success(detail);
    }
}
