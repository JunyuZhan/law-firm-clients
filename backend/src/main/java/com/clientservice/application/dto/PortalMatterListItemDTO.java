package com.clientservice.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户门户事项列表项 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalMatterListItemDTO {

    /** 项目 ID */
    private String id;

    /** 客户名称 */
    private String clientName;

    /** 项目名称 */
    private String matterName;

    /** 项目状态 */
    private String status;

    /** 项目状态名称 */
    private String statusName;

    /** 承办律师 */
    private String counsel;

    /** 有效期 */
    private LocalDateTime expiresAt;

    /** 访问令牌 */
    private String accessToken;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
