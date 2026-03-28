package com.clientservice.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API密钥更新请求DTO
 */
@Data
public class ApiKeyUpdateRequest {
    
    /**
     * 密钥名称（可选）
     */
    @Size(max = 100, message = "密钥名称长度不能超过100个字符")
    private String keyName;
    
    /**
     * 律所名称（可选）
     */
    @Size(max = 200, message = "律所名称长度不能超过200个字符")
    private String lawFirmName;
    
    /**
     * 是否启用（可选）
     */
    private Boolean enabled;
    
    /**
     * 过期时间（可选）
     */
    private LocalDateTime expiresAt;
}
