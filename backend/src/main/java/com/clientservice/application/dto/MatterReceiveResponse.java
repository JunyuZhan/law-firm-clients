package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接收项目数据响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatterReceiveResponse {

    /** 项目ID（外部系统ID） */
    private String id;

    /** 客户访问链接 */
    private String accessUrl;
}
