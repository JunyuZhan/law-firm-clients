package com.clientservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API密钥创建请求DTO
 */
@Data
public class ApiKeyCreateRequest {
    
    /**
     * 密钥名称（必填）
     */
    @NotBlank(message = "密钥名称不能为空")
    @Size(max = 100, message = "密钥名称长度不能超过100个字符")
    private String keyName;
    
    /**
     * 律所名称（可选）
     */
    @Size(max = 200, message = "律所名称长度不能超过200个字符")
    private String lawFirmName;
    
    /**
     * 过期时间（可选，null表示永不过期）
     */
    private LocalDateTime expiresAt;
}
