package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知模板数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    /** 项目名称 */
    private String matterName;

    /** 客户名称 */
    private String clientName;

    /** 访问链接 */
    private String accessUrl;

    /** 有效期（天） */
    private Integer validDays;

    /** 其他自定义数据 */
    private String customData;
}
