package com.clientservice.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户门户通知记录项 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalNotificationItemDTO {

    /** 主键 */
    private Long id;

    /** 项目 ID */
    private String matterId;

    /** 项目名称 */
    private String matterName;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型 */
    private String notificationType;

    /** 发送状态 */
    private String status;

    /** 接收人 */
    private String recipient;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
