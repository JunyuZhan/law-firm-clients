package com.clientservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通知模板创建请求DTO
 */
@Data
public class NotificationTemplateCreateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    private String templateCode;

    private String templateContent;

    private String templateVariables;

    private String provider;

    private String signName;

    private Boolean enabled;

    private String description;
}
