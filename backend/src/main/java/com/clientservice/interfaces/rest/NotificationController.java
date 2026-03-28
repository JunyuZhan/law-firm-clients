package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.NotificationHistoryDTO;
import com.clientservice.application.service.MatterService;
import com.clientservice.application.service.NotificationRecordService;
import com.clientservice.application.service.NotificationService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ClientMatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知控制器（管理后台使用，JWT认证）
 */
@Slf4j
@Tag(name = "通知管理", description = "通知发送和历史查询接口（管理后台）")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final MatterService matterService;
    private final NotificationService notificationService;
    private final NotificationRecordService notificationRecordService;

    /**
     * 手动发送通知
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param matterId 项目ID
     * @return 空结果
     */
    @Operation(summary = "手动发送通知", description = "手动触发项目通知发送（管理后台使用，JWT认证）")
    @PostMapping("/send")
    public Result<Void> sendNotification(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId) {

        // 获取项目
        ClientMatter matter = matterService.getMatterById(matterId);

        // 异步发送通知
        notificationService.sendNotificationAsync(matter);

        log.info("手动触发通知发送: matterId={}", matterId);

        return Result.success();
    }

    /**
     * 获取通知历史
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param notificationType 通知类型（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 通知历史列表
     */
    @Operation(summary = "获取通知历史", description = "查询通知发送历史记录（管理后台使用，JWT认证）")
    @GetMapping("/history")
    public Result<List<NotificationHistoryDTO>> getNotificationHistory(
            @Parameter(description = "项目ID") @RequestParam(required = false) final String matterId,
            @Parameter(description = "客户ID") @RequestParam(required = false) final Long clientId,
            @Parameter(description = "通知类型") @RequestParam(required = false) final String notificationType,
            @Parameter(description = "状态") @RequestParam(required = false) final String status,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endTime,
            @Parameter(description = "限制数量") @RequestParam(required = false) final Integer limit) {

        // 获取通知历史
        List<NotificationHistoryDTO> history = notificationRecordService.getNotificationHistory(
                matterId, clientId, notificationType, status, startTime, endTime, limit);

        return Result.success(history);
    }

    /**
     * 获取通知统计信息
     * 
     * 注意：此接口由JWT过滤器保护，无需在Controller中验证API密钥
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     */
    @Operation(summary = "获取通知统计", description = "获取通知发送统计信息（管理后台使用，JWT认证）")
    @GetMapping("/statistics")
    public Result<com.clientservice.application.dto.NotificationStatisticsDTO> getNotificationStatistics(
            @Parameter(description = "项目ID") @RequestParam(required = false) final String matterId,
            @Parameter(description = "客户ID") @RequestParam(required = false) final Long clientId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endTime) {

        // 获取统计信息
        com.clientservice.application.dto.NotificationStatisticsDTO statistics = 
                notificationRecordService.getNotificationStatistics(matterId, clientId, startTime, endTime);

        return Result.success(statistics);
    }
}
