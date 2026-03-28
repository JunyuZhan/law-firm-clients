package com.clientservice.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函件验证数据接收DTO
 * 接收管理系统推送的 LetterVerificationPushDTO 数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterVerificationReceiveDTO {

    /** 函件申请ID（内部关联用） */
    @NotNull(message = "函件申请ID不能为空")
    private Long letterId;

    /** 函件申请编号（对外展示） */
    @NotBlank(message = "函件申请编号不能为空")
    private String applicationNo;

    /** 验证码（用于验证） */
    @NotBlank(message = "验证码不能为空")
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    /** 打印/盖章时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime printedAt;

    /** 有效期截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validUntil;

    /** 备注 */
    private String remark;
}
