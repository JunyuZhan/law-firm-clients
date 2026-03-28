package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 项目详情DTO（管理后台使用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatterDetailDTO {

    /** 项目ID */
    private String id;

    /** 律所系统项目ID */
    private Long lawFirmMatterId;

    /** 客户ID */
    private Long clientId;

    /** 客户名称 */
    private String clientName;

    /** 项目数据（JSON格式，反序列化为Map） */
    private Map<String, Object> matterData;

    /** 权限范围 */
    private String scopes;

    /** 状态：ACTIVE、EXPIRED、REVOKED */
    private String status;

    /** 有效期（天） */
    private Integer validDays;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 访问令牌 */
    private String accessToken;

    /** 访问链接 */
    private String accessUrl;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
