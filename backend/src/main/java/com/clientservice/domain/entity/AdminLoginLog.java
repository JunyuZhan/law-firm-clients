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
 * 管理员登录日志实体 - 记录管理员登录历史
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("admin_login_log")
public class AdminLoginLog extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（外键关联admin_user.id） */
    private Long userId;

    /** 用户名（冗余字段，便于查询） */
    private String username;

    /** 登录IP地址 */
    private String ipAddress;

    /** 用户代理 */
    private String userAgent;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 是否成功 */
    private Boolean success;

    /** 失败原因（可选） */
    private String failureReason;

    // ========== 失败原因常量 ==========
    /** 失败原因：用户名不存在 */
    public static final String FAILURE_USERNAME_NOT_FOUND = "USERNAME_NOT_FOUND";

    /** 失败原因：密码错误 */
    public static final String FAILURE_PASSWORD_ERROR = "PASSWORD_ERROR";

    /** 失败原因：账户已锁定 */
    public static final String FAILURE_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

    /** 失败原因：账户已禁用 */
    public static final String FAILURE_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
}
