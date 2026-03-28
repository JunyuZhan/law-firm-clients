package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {
    /** 验证码ID（客户端需要保存，提交登录时一起发送） */
    private String captchaId;

    /** 验证码图片（Base64编码） */
    private String captchaImage;
}
