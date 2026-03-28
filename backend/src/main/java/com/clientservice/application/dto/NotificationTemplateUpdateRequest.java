package com.clientservice.application.dto;

import lombok.Data;

/**
 * 通知模板更新请求DTO
 */
@Data
public class NotificationTemplateUpdateRequest {

    private String templateName;

    private String templateContent;

    private String templateVariables;

    private Boolean enabled;

    private String description;
}
