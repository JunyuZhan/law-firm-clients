package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.LetterVerificationReceiveDTO;
import com.clientservice.application.dto.LetterVerificationResultDTO;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.LetterVerificationService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ApiKey;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 函件验证控制器
 */
@Slf4j
@Tag(name = "函件验证", description = "函件真伪验证相关接口")
@RestController
@RequestMapping({"/api/letter/verification", "/letter/verification"})
@RequiredArgsConstructor
public class LetterVerificationController {

    private final LetterVerificationService letterVerificationService;
    private final ApiKeyService apiKeyService;

    /**
     * 接收推送数据（需要API Key认证）
     *
     * @param authorization 认证头（Bearer Token - API Key）
     * @param dto 验证数据
     * @param request HTTP请求
     * @return 操作结果，包含验证URL
     */
    @Operation(summary = "接收验证数据", description = "接收律所管理系统推送的函件验证数据（需要API Key认证）")
    @Timed(value = "api.letter.verification.receive", description = "接收函件验证数据接口耗时")
    @PostMapping("/receive")
    public Result<java.util.Map<String, String>> receiveVerificationData(
            @RequestHeader(value = "Authorization") final String authorization,
            @Valid @RequestBody final LetterVerificationReceiveDTO dto,
            final HttpServletRequest request) {

        ApiKey apiKey = apiKeyService.validateApiKey(authorization);
        log.info("API密钥验证成功: keyName={}, lawFirmName={}", apiKey.getKeyName(), apiKey.getLawFirmName());

        letterVerificationService.receiveVerificationData(dto);

        String verifyUrl = letterVerificationService.generateVerifyUrl(dto.getApplicationNo(), request);
        
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("verifyUrl", verifyUrl);

        log.info("接收函件验证数据成功: applicationNo={}, verifyUrl={}", dto.getApplicationNo(), verifyUrl);
        return Result.success(data);
    }

    /**
     * 验证函件（公开接口）
     * 
     * 支持两种参数名：
     * - no/code: 二维码扫描场景（管理系统生成的URL）
     * - applicationNo/code: 兼容旧版本
     *
     * @param no 函件申请编号（二维码URL使用）
     * @param applicationNo 函件申请编号（兼容旧版本）
     * @param code 验证码
     * @param request HTTP请求
     * @return 验证结果
     */
    @Operation(summary = "验证函件", description = "验证函件真伪（公开接口，无需认证）")
    @Timed(value = "api.letter.verification.verify", description = "验证函件接口耗时")
    @GetMapping("/verify")
    public Result<LetterVerificationResultDTO> verifyLetter(
            @Parameter(description = "函件申请编号（二维码URL使用）") @RequestParam(required = false) final String no,
            @Parameter(description = "函件申请编号（兼容旧版本）") @RequestParam(required = false) final String applicationNo,
            @Parameter(description = "验证码", required = true) @RequestParam final String code,
            final HttpServletRequest request) {

        String letterNo = no != null ? no : applicationNo;
        if (letterNo == null || letterNo.isEmpty()) {
            return Result.success(LetterVerificationResultDTO.builder()
                    .valid(false)
                    .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_NOT_FOUND)
                    .statusMessage("函件编号不能为空")
                    .build());
        }

        log.info("验证函件请求: no={}", letterNo);

        LetterVerificationResultDTO result = letterVerificationService.verifyLetter(letterNo, code, request);

        return Result.success(result);
    }

    /**
     * 获取验证信息（公开接口）
     *
     * @param applicationNo 函件申请编号
     * @return 验证信息
     */
    @Operation(summary = "获取验证信息", description = "获取函件验证信息（公开接口，无需认证）")
    @Timed(value = "api.letter.verification.info", description = "获取函件验证信息接口耗时")
    @GetMapping("/info/{applicationNo}")
    public Result<LetterVerificationResultDTO> getVerificationInfo(
            @Parameter(description = "函件申请编号", required = true) @PathVariable final String applicationNo) {

        log.info("获取验证信息请求: applicationNo={}", applicationNo);

        LetterVerificationResultDTO result = letterVerificationService.getVerificationInfo(applicationNo);

        return Result.success(result);
    }

    /**
     * 撤销验证（管理端接口，需要API Key认证）
     *
     * @param authorization 认证头（Bearer Token - API Key）
     * @param letterId 律所系统函件ID
     * @return 操作结果
     */
    @Operation(summary = "撤销验证", description = "撤销函件验证（管理端接口，需要API Key认证）")
    @Timed(value = "api.letter.verification.revoke", description = "撤销函件验证接口耗时")
    @DeleteMapping("/{letterId}")
    public Result<Void> revokeVerification(
            @RequestHeader(value = "Authorization") final String authorization,
            @Parameter(description = "律所系统函件ID", required = true) @PathVariable final Long letterId) {

        ApiKey apiKey = apiKeyService.validateApiKey(authorization);
        log.info("API密钥验证成功: keyName={}", apiKey.getKeyName());

        letterVerificationService.revokeVerification(letterId);

        log.info("撤销函件验证成功: letterId={}", letterId);
        return Result.success("撤销成功", null);
    }
}
