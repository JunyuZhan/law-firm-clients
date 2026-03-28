package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知历史DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryDTO {

    /** 主键 */
    private Long id;

    /** 项目ID */
    private String matterId;

    /** 客户ID */
    private Long clientId;

    /** 通知类型：SMS、WECHAT、EMAIL */
    private String notificationType;

    /** 接收人（手机号/微信号/邮箱） */
    private String recipient;

    /** 通知内容 */
    private String content;

    /** 状态：PENDING（待发送）、SUCCESS（成功）、FAILED（失败） */
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetries;

    /** 下次重试时间 */
    private LocalDateTime nextRetryAt;

    /** 最后重试时间 */
    private LocalDateTime lastRetryAt;
}
