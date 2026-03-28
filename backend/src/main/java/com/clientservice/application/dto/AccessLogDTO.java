package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 访问日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLogDTO {

    /** 主键 */
    private Long id;

    /** 项目ID */
    private String matterId;

    /** 客户ID */
    private Long clientId;

    /** 访问令牌 */
    private String accessToken;

    /** IP地址 */
    private String ipAddress;

    /** 用户代理 */
    private String userAgent;

    /** 访问时间 */
    private LocalDateTime accessTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
