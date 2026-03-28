package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatisticsDTO {

    /** 总数 */
    private Long total;

    /** 成功数 */
    private Long success;

    /** 失败数 */
    private Long failed;

    /** 待发送数 */
    private Long pending;

    /** 按类型统计 */
    private TypeStatistics sms;
    private TypeStatistics wechat;
    private TypeStatistics email;

    /**
     * 类型统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStatistics {
        private Long total;
        private Long success;
        private Long failed;
        private Long pending;
    }
}
