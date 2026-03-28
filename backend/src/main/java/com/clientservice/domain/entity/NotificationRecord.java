package com.clientservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clientservice.common.base.BaseEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 通知记录实体 - 记录通知发送历史
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("notification_record")
public class NotificationRecord extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目ID */
    private String matterId;

    /** 客户ID */
    private Long clientId;

    /** 通知类型：SMS、WECHAT、EMAIL */
    private String notificationType;

    /** 接收人（手机号/微信号/邮箱） */
    private String recipient;

    /** 通知内容 */
    private String content;

    /** 状态：PENDING（待发送）、SUCCESS（成功）、FAILED（失败） */
    private String status;

    /** 错误分类：DATA_NOT_FOUND、DATA_EMPTY、SEND_FAILED、CHANNEL_DISABLED */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数（默认3次） */
    private Integer maxRetries;

    /** 下次重试时间 */
    private LocalDateTime nextRetryAt;

    /** 最后重试时间 */
    private LocalDateTime lastRetryAt;

    /** 律所系统项目ID（用于回调） */
    private Long lawFirmMatterId;

    /** 客户名称（用于回调） */
    private String clientName;

    // ========== 通知类型常量 ==========
    /** 通知类型：短信 */
    public static final String TYPE_SMS = "SMS";

    /** 通知类型：微信 */
    public static final String TYPE_WECHAT = "WECHAT";

    /** 通知类型：邮件 */
    public static final String TYPE_EMAIL = "EMAIL";

    // ========== 状态常量 ==========
    /** 状态：待发送 */
    public static final String STATUS_PENDING = "PENDING";

    /** 状态：成功 */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /** 状态：失败 */
    public static final String STATUS_FAILED = "FAILED";

    // ========== 错误分类常量 ==========
    /** 错误分类：联系方式未找到 */
    public static final String ERROR_DATA_NOT_FOUND = "DATA_NOT_FOUND";

    /** 错误分类：联系方式为空 */
    public static final String ERROR_DATA_EMPTY = "DATA_EMPTY";

    /** 错误分类：发送失败（包括格式错误、渠道故障等） */
    public static final String ERROR_SEND_FAILED = "SEND_FAILED";

    /** 错误分类：渠道未启用 */
    public static final String ERROR_CHANNEL_DISABLED = "CHANNEL_DISABLED";
}
