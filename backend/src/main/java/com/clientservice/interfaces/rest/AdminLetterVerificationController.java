package com.clientservice.interfaces.rest;

import com.clientservice.application.service.LetterVerificationService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.LetterVerification;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 函件验证管理控制器（管理后台）
 */
@Slf4j
@Tag(name = "函件验证管理", description = "管理后台-函件验证管理接口")
@RestController
@RequestMapping("/api/admin/letter-verification")
@RequiredArgsConstructor
public class AdminLetterVerificationController {

    private final LetterVerificationService letterVerificationService;

    /**
     * 分页查询验证记录
     *
     * @param page 当前页（从1开始）
     * @param pageSize 每页大小
     * @param status 状态筛选
     * @param keyword 关键词搜索
     * @return 分页结果
     */
    @Operation(summary = "分页查询验证记录", description = "分页查询函件验证记录")
    @Timed(value = "api.admin.letter.verification.list", description = "分页查询验证记录接口耗时")
    @GetMapping("/list")
    public Result<Map<String, Object>> getVerificationList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("查询验证记录: page={}, pageSize={}, status={}, keyword={}", page, pageSize, status, keyword);

        Map<String, Object> result = letterVerificationService.getVerificationList(page, pageSize, status, keyword);

        return Result.success(result);
    }

    /**
     * 获取验证详情
     *
     * @param id 验证记录ID
     * @return 验证记录
     */
    @Operation(summary = "获取验证详情", description = "根据ID获取验证记录详情")
    @Timed(value = "api.admin.letter.verification.detail", description = "获取验证详情接口耗时")
    @GetMapping("/{id}")
    public Result<LetterVerification> getVerificationDetail(
            @Parameter(description = "验证记录ID", required = true) @PathVariable Long id) {

        log.info("获取验证详情: id={}", id);

        LetterVerification result = letterVerificationService.getVerificationById(id);

        return Result.success(result);
    }

    /**
     * 撤销验证
     *
     * @param id 验证记录ID
     * @return 操作结果
     */
    @Operation(summary = "撤销验证", description = "撤销函件验证")
    @Timed(value = "api.admin.letter.verification.revoke", description = "撤销验证接口耗时")
    @DeleteMapping("/{id}")
    public Result<Void> revokeVerification(
            @Parameter(description = "验证记录ID", required = true) @PathVariable Long id) {

        log.info("撤销验证: id={}", id);

        letterVerificationService.revokeVerificationById(id);

        return Result.success("撤销成功", null);
    }

    /**
     * 获取验证统计数据
     *
     * @return 统计数据
     */
    @Operation(summary = "获取验证统计", description = "获取函件验证统计数据")
    @Timed(value = "api.admin.letter.verification.statistics", description = "获取验证统计接口耗时")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getVerificationStatistics() {

        log.info("获取验证统计");

        Map<String, Object> result = letterVerificationService.getVerificationStatistics();

        return Result.success(result);
    }
}
