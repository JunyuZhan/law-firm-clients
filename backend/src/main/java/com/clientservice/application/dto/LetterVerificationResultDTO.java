package com.clientservice.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函件验证结果DTO
 * 用于返回验证结果给前端展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterVerificationResultDTO {

    /** 是否有效 */
    private Boolean valid;

    /** 验证状态：VALID-有效 / INVALID-无效 / EXPIRED-已过期 / REVOKED-已撤销 / NOT_FOUND-未找到 */
    private String verifyStatus;

    /** 状态提示信息 */
    private String statusMessage;

    /** 函件申请编号 */
    private String applicationNo;

    /** 函件类型名称 */
    private String letterTypeName;

    /** 律师事务所名称 */
    private String firmName;

    /** 出函律师姓名 */
    private String lawyerNames;

    /** 接收单位 */
    private String targetUnit;

    /** 关联项目名称 */
    private String matterName;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    /** 打印/盖章时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printedAt;

    /** 有效期截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    /** 验证次数 */
    private Integer verifyCount;

    /** 备注 */
    private String remark;

    // ========== 验证状态常量 ==========
    public static final String VERIFY_STATUS_VALID = "VALID";
    public static final String VERIFY_STATUS_INVALID = "INVALID";
    public static final String VERIFY_STATUS_EXPIRED = "EXPIRED";
    public static final String VERIFY_STATUS_REVOKED = "REVOKED";
    public static final String VERIFY_STATUS_NOT_FOUND = "NOT_FOUND";
}
