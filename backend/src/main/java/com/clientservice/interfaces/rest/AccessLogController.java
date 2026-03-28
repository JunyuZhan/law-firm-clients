package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.AccessLogDTO;
import com.clientservice.application.service.AccessLogService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ClientMatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访问日志控制器
 */
@Slf4j
@Tag(name = "访问日志", description = "访问日志查询接口")
@RestController
@RequestMapping("/api/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;
    private final MatterService matterService;

    /**
     * 获取访问历史
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 访问日志列表
     */
    @Operation(summary = "获取访问历史", description = "获取项目的访问历史记录")
    @GetMapping
    public Result<List<AccessLogDTO>> getAccessHistory(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endTime,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return Result.forbidden("项目ID不匹配");
        }

        // 获取访问历史
        List<AccessLogDTO> logs = accessLogService.getAccessHistory(
                matterId, matter.getClientId(), startTime, endTime, limit);

        return Result.success(logs);
    }

    /**
     * 获取访问统计
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 访问统计信息
     */
    @Operation(summary = "获取访问统计", description = "获取项目的访问统计数据")
    @GetMapping("/statistics")
    public Result<AccessLogService.AccessStatistics> getAccessStatistics(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endTime) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return Result.forbidden("项目ID不匹配");
        }

        // 获取访问统计
        AccessLogService.AccessStatistics statistics = accessLogService.getAccessStatistics(
                matterId, matter.getClientId(), startTime, endTime);

        return Result.success(statistics);
    }
}
