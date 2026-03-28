package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知模板DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDTO {

    /** 主键 */
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板类型：SMS、WECHAT、EMAIL */
    private String templateType;

    /** 模板代码/ID */
    private String templateCode;

    /** 模板内容 */
    private String templateContent;

    /** 模板变量说明 */
    private String templateVariables;

    /** 提供商 */
    private String provider;

    /** 签名名称 */
    private String signName;

    /** 是否启用 */
    private Boolean enabled;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
