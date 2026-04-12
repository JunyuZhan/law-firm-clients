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
 * 函件验证实体 - 存储律所系统推送的电子函件验证信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("letter_verification")
public class LetterVerification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 律所系统函件ID */
    private Long letterId;

    /** 来源 API Key ID */
    private Long sourceApiKeyId;

    /** 函件申请编号 */
    private String applicationNo;

    /** 验证码 */
    private String verificationCode;

    /** 函件类型编码 */
    private String letterType;

    /** 函件类型名称 */
    private String letterTypeName;

    /** 接收单位 */
    private String targetUnit;

    /** 出函律师姓名（多人逗号分隔） */
    private String lawyerNames;

    /** 律师事务所名称 */
    private String firmName;

    /** 关联项目名称（脱敏） */
    private String matterName;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 打印/盖章时间 */
    private LocalDateTime printedAt;

    /** 有效期截止时间 */
    private LocalDateTime validUntil;

    /** 备注 */
    private String remark;

    /** 验证次数 */
    private Integer verifyCount;

    /** 最后验证时间 */
    private LocalDateTime lastVerifyAt;

    /** 最后验证IP */
    private String lastVerifyIp;

    /** 状态 */
    private String status;

    // ========== 状态常量 ==========
    /** 状态：有效 */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** 状态：已过期 */
    public static final String STATUS_EXPIRED = "EXPIRED";

    /** 状态：已撤销 */
    public static final String STATUS_REVOKED = "REVOKED";
}
