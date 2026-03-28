package com.clientservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clientservice.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 通知模板实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("notification_template")
public class NotificationTemplate extends BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板类型：SMS、WECHAT、EMAIL */
    private String templateType;

    /** 模板代码/ID（第三方平台的模板ID） */
    private String templateCode;

    /** 模板内容（JSON格式，包含变量占位符） */
    private String templateContent;

    /** 模板变量说明（JSON格式，描述变量含义） */
    private String templateVariables;

    /** 提供商：aliyun、tencent、wechat等 */
    private String provider;

    /** 签名名称（短信需要） */
    private String signName;

    /** 是否启用 */
    private Boolean enabled;

    /** 描述 */
    private String description;

    // ========== 模板类型常量 ==========
    /** 模板类型：短信 */
    public static final String TYPE_SMS = "SMS";

    /** 模板类型：微信 */
    public static final String TYPE_WECHAT = "WECHAT";

    /** 模板类型：邮件 */
    public static final String TYPE_EMAIL = "EMAIL";

    // ========== 提供商常量 ==========
    /** 提供商：阿里云 */
    public static final String PROVIDER_ALIYUN = "aliyun";

    /** 提供商：腾讯云 */
    public static final String PROVIDER_TENCENT = "tencent";

    /** 提供商：微信 */
    public static final String PROVIDER_WECHAT = "wechat";
}
