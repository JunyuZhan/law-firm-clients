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
 * 回调出站任务 - 持久化发往主系统的回调事件。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("callback_task")
public class CallbackTask extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_DEAD = "DEAD";

    public static final String TYPE_ACCESS_LOG = "ACCESS_LOG";
    public static final String TYPE_DOWNLOAD_LOG = "DOWNLOAD_LOG";
    public static final String TYPE_FILE_UPLOAD = "FILE_UPLOAD";
    public static final String TYPE_NOTIFICATION_SUCCESS = "NOTIFICATION_SUCCESS";
    public static final String TYPE_NOTIFICATION_FAILURE = "NOTIFICATION_FAILURE";

    @TableId(type = IdType.AUTO)
    private Long id;

    private String matterId;

    private String callbackType;

    private String callbackUrl;

    private String callbackPayload;

    private String status;

    private Integer retryCount;

    private Integer maxRetries;

    private LocalDateTime nextRetryAt;

    private LocalDateTime lastAttemptAt;

    private String lastError;
}
