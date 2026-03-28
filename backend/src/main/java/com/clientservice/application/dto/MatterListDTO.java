package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目列表DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatterListDTO {

    /** 项目ID */
    private String id;

    /** 律所系统项目ID */
    private Long lawFirmMatterId;

    /** 客户ID */
    private Long clientId;

    /** 客户名称 */
    private String clientName;

    /** 状态：ACTIVE、EXPIRED、REVOKED */
    private String status;

    /** 有效期（天） */
    private Integer validDays;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 访问链接 */
    private String accessUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
