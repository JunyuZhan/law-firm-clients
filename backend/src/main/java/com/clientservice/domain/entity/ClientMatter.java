package com.clientservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clientservice.common.base.BaseEntity;
import com.clientservice.infrastructure.persistence.typehandler.PostgresJsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 项目数据实体 - 存储从律所管理系统推送的项目数据
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "client_matter", autoResultMap = true)
public class ClientMatter extends BaseEntity {

    /** 主键，外部系统ID */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 律所系统项目ID */
    private Long lawFirmMatterId;

    /** 客户ID */
    private Long clientId;

    /** 客户名称 */
    private String clientName;

    /** 来源 API Key ID */
    private Long sourceApiKeyId;

    /** 项目数据（完整JSON） */
    @TableField(typeHandler = PostgresJsonTypeHandler.class)
    private String matterData;

    /** 数据范围（逗号分隔） */
    private String scopes;

    /** 有效期（天） */
    private Integer validDays;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expiresAt;

    /** 访问令牌（用于生成访问链接） */
    private String accessToken;

    /** 客户访问链接 */
    private String accessUrl;

    /** 状态：ACTIVE（有效）、EXPIRED（已过期）、REVOKED（已撤销） */
    private String status;

    // ========== 状态常量 ==========
    /** 状态：有效 */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** 状态：已过期 */
    public static final String STATUS_EXPIRED = "EXPIRED";

    /** 状态：已撤销 */
    public static final String STATUS_REVOKED = "REVOKED";
}
