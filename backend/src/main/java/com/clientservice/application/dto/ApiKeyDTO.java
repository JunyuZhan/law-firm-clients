package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API密钥DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDTO {

    /** 主键 */
    private Long id;

    /** 密钥名称 */
    private String keyName;

    /** API密钥（显示时部分隐藏） */
    private String apiKey;

    /** API密钥Secret（可选，显示时部分隐藏） */
    private String apiSecret;

    /** 律所名称 */
    private String lawFirmName;

    /** 是否启用 */
    private Boolean enabled;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
