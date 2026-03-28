package com.clientservice.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clientservice.application.dto.LetterVerificationReceiveDTO;
import com.clientservice.application.dto.LetterVerificationResultDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.LetterVerification;
import com.clientservice.infrastructure.persistence.mapper.LetterVerificationMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 函件验证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LetterVerificationService {

    private final LetterVerificationMapper letterVerificationMapper;
    private final SysConfigService sysConfigService;

    /**
     * 接收并存储验证数据
     *
     * @param dto 验证数据DTO
     */
    @Transactional
    public void receiveVerificationData(final LetterVerificationReceiveDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证数据不能为空");
        }

        LetterVerification existing = letterVerificationMapper.selectByApplicationNo(dto.getApplicationNo());
        
        if (existing != null) {
            existing.setVerificationCode(dto.getVerificationCode());
            existing.setLetterType(dto.getLetterType());
            existing.setLetterTypeName(dto.getLetterTypeName());
            existing.setTargetUnit(dto.getTargetUnit());
            existing.setLawyerNames(dto.getLawyerNames());
            existing.setFirmName(dto.getFirmName());
            existing.setMatterName(dto.getMatterName());
            existing.setApprovedAt(dto.getApprovedAt());
            existing.setPrintedAt(dto.getPrintedAt());
            existing.setValidUntil(dto.getValidUntil());
            existing.setRemark(dto.getRemark());
            existing.setStatus(LetterVerification.STATUS_ACTIVE);
            letterVerificationMapper.updateById(existing);
            log.info("更新函件验证数据: applicationNo={}", dto.getApplicationNo());
        } else {
            LetterVerification entity = LetterVerification.builder()
                    .letterId(dto.getLetterId())
                    .applicationNo(dto.getApplicationNo())
                    .verificationCode(dto.getVerificationCode())
                    .letterType(dto.getLetterType())
                    .letterTypeName(dto.getLetterTypeName())
                    .targetUnit(dto.getTargetUnit())
                    .lawyerNames(dto.getLawyerNames())
                    .firmName(dto.getFirmName())
                    .matterName(dto.getMatterName())
                    .approvedAt(dto.getApprovedAt())
                    .printedAt(dto.getPrintedAt())
                    .validUntil(dto.getValidUntil())
                    .remark(dto.getRemark())
                    .verifyCount(0)
                    .status(LetterVerification.STATUS_ACTIVE)
                    .build();
            letterVerificationMapper.insert(entity);
            log.info("创建函件验证数据: applicationNo={}, letterId={}", dto.getApplicationNo(), dto.getLetterId());
        }
    }

    /**
     * 生成验证URL
     *
     * @param applicationNo 函件申请编号
     * @param request HTTP请求（用于获取基础URL）
     * @return 验证URL
     */
    public String generateVerifyUrl(final String applicationNo, final HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        try {
            String encodedNo = java.net.URLEncoder.encode(applicationNo, java.nio.charset.StandardCharsets.UTF_8);
            return baseUrl + "/verify/letter?no=" + encodedNo;
        } catch (Exception e) {
            log.warn("URL编码失败: applicationNo={}", applicationNo, e);
            return baseUrl + "/verify/letter?no=" + applicationNo;
        }
    }

    /**
     * 获取基础URL（优先从系统配置读取）
     */
    private String getBaseUrl(final HttpServletRequest request) {
        String configuredUrl = sysConfigService.getConfigValue("system.base-url", null);
        if (configuredUrl != null && !configuredUrl.isEmpty()) {
            return configuredUrl.endsWith("/") ? configuredUrl.substring(0, configuredUrl.length() - 1) : configuredUrl;
        }
        
        if (request == null) {
            return "";
        }
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }

    /**
     * 验证函件真伪
     *
     * @param applicationNo 函件申请编号
     * @param verificationCode 验证码
     * @param request HTTP请求（用于获取IP）
     * @return 验证结果DTO
     */
    @Transactional
    public LetterVerificationResultDTO verifyLetter(final String applicationNo, final String verificationCode, 
                                                     final HttpServletRequest request) {
        if (!StringUtils.hasText(applicationNo)) {
            return buildNotFoundResult("函件编号不能为空");
        }
        if (!StringUtils.hasText(verificationCode)) {
            return buildInvalidResult("验证码不能为空");
        }

        LetterVerification entity = letterVerificationMapper.selectByApplicationNo(applicationNo);
        
        if (entity == null) {
            log.warn("函件验证失败: 未找到函件, applicationNo={}", applicationNo);
            return buildNotFoundResult("未找到该函件信息");
        }

        String clientIp = getClientIpAddress(request);
        letterVerificationMapper.updateVerifyInfo(entity.getId(), clientIp);
        
        entity.setVerifyCount(entity.getVerifyCount() != null ? entity.getVerifyCount() + 1 : 1);

        if (!verificationCode.equals(entity.getVerificationCode())) {
            log.warn("函件验证失败: 验证码错误, applicationNo={}", applicationNo);
            return buildInvalidResultWithInfo(entity, "验证码错误，该函件可能为伪造");
        }

        if (LetterVerification.STATUS_REVOKED.equals(entity.getStatus())) {
            log.warn("函件验证失败: 已撤销, applicationNo={}", applicationNo);
            return buildRevokedResult(entity);
        }

        if (entity.getValidUntil() != null && entity.getValidUntil().isBefore(LocalDateTime.now())) {
            log.warn("函件验证失败: 已过期, applicationNo={}, validUntil={}", applicationNo, entity.getValidUntil());
            return buildExpiredResult(entity);
        }

        log.info("函件验证成功: applicationNo={}, verifyCount={}", applicationNo, entity.getVerifyCount());
        return buildValidResult(entity);
    }

    /**
     * 获取验证信息（用于页面展示）
     *
     * @param applicationNo 函件申请编号
     * @return 验证结果DTO
     */
    public LetterVerificationResultDTO getVerificationInfo(final String applicationNo) {
        if (!StringUtils.hasText(applicationNo)) {
            return buildNotFoundResult("函件编号不能为空");
        }

        LetterVerification entity = letterVerificationMapper.selectByApplicationNo(applicationNo);
        
        if (entity == null) {
            return buildNotFoundResult("未找到该函件信息");
        }

        return buildInfoResult(entity);
    }

    /**
     * 撤销验证（管理端调用）
     *
     * @param letterId 律所系统函件ID
     */
    @Transactional
    public void revokeVerification(final Long letterId) {
        if (letterId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "函件ID不能为空");
        }

        LetterVerification entity = letterVerificationMapper.selectByLetterId(letterId);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该函件验证信息");
        }

        letterVerificationMapper.revokeByLetterId(letterId);
        log.info("撤销函件验证: letterId={}, applicationNo={}", letterId, entity.getApplicationNo());
    }

    private LetterVerificationResultDTO buildValidResult(LetterVerification entity) {
        return LetterVerificationResultDTO.builder()
                .valid(true)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_VALID)
                .statusMessage("验证通过，该函件真实有效")
                .applicationNo(entity.getApplicationNo())
                .letterTypeName(entity.getLetterTypeName())
                .firmName(entity.getFirmName())
                .lawyerNames(entity.getLawyerNames())
                .targetUnit(entity.getTargetUnit())
                .matterName(entity.getMatterName())
                .approvedAt(entity.getApprovedAt())
                .printedAt(entity.getPrintedAt())
                .validUntil(entity.getValidUntil())
                .verifyCount(entity.getVerifyCount())
                .remark(entity.getRemark())
                .build();
    }

    private LetterVerificationResultDTO buildExpiredResult(LetterVerification entity) {
        return LetterVerificationResultDTO.builder()
                .valid(false)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_EXPIRED)
                .statusMessage("该函件已过期")
                .applicationNo(entity.getApplicationNo())
                .letterTypeName(entity.getLetterTypeName())
                .firmName(entity.getFirmName())
                .lawyerNames(entity.getLawyerNames())
                .targetUnit(entity.getTargetUnit())
                .matterName(entity.getMatterName())
                .approvedAt(entity.getApprovedAt())
                .printedAt(entity.getPrintedAt())
                .validUntil(entity.getValidUntil())
                .verifyCount(entity.getVerifyCount())
                .remark(entity.getRemark())
                .build();
    }

    private LetterVerificationResultDTO buildRevokedResult(LetterVerification entity) {
        return LetterVerificationResultDTO.builder()
                .valid(false)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_REVOKED)
                .statusMessage("该函件已被撤销")
                .applicationNo(entity.getApplicationNo())
                .letterTypeName(entity.getLetterTypeName())
                .firmName(entity.getFirmName())
                .lawyerNames(entity.getLawyerNames())
                .targetUnit(entity.getTargetUnit())
                .matterName(entity.getMatterName())
                .approvedAt(entity.getApprovedAt())
                .printedAt(entity.getPrintedAt())
                .validUntil(entity.getValidUntil())
                .verifyCount(entity.getVerifyCount())
                .remark(entity.getRemark())
                .build();
    }

    private LetterVerificationResultDTO buildInvalidResult(String message) {
        return LetterVerificationResultDTO.builder()
                .valid(false)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_INVALID)
                .statusMessage(message)
                .build();
    }

    private LetterVerificationResultDTO buildInvalidResultWithInfo(LetterVerification entity, String message) {
        return LetterVerificationResultDTO.builder()
                .valid(false)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_INVALID)
                .statusMessage(message)
                .applicationNo(entity.getApplicationNo())
                .letterTypeName(entity.getLetterTypeName())
                .firmName(entity.getFirmName())
                .verifyCount(entity.getVerifyCount())
                .build();
    }

    private LetterVerificationResultDTO buildNotFoundResult(String message) {
        return LetterVerificationResultDTO.builder()
                .valid(false)
                .verifyStatus(LetterVerificationResultDTO.VERIFY_STATUS_NOT_FOUND)
                .statusMessage(message)
                .build();
    }

    private LetterVerificationResultDTO buildInfoResult(LetterVerification entity) {
        String verifyStatus;
        String statusMessage;
        boolean valid;

        if (LetterVerification.STATUS_REVOKED.equals(entity.getStatus())) {
            verifyStatus = LetterVerificationResultDTO.VERIFY_STATUS_REVOKED;
            statusMessage = "该函件已被撤销";
            valid = false;
        } else if (entity.getValidUntil() != null && entity.getValidUntil().isBefore(LocalDateTime.now())) {
            verifyStatus = LetterVerificationResultDTO.VERIFY_STATUS_EXPIRED;
            statusMessage = "该函件已过期";
            valid = false;
        } else {
            verifyStatus = LetterVerificationResultDTO.VERIFY_STATUS_VALID;
            statusMessage = "该函件有效";
            valid = true;
        }

        return LetterVerificationResultDTO.builder()
                .valid(valid)
                .verifyStatus(verifyStatus)
                .statusMessage(statusMessage)
                .applicationNo(entity.getApplicationNo())
                .letterTypeName(entity.getLetterTypeName())
                .firmName(entity.getFirmName())
                .lawyerNames(entity.getLawyerNames())
                .targetUnit(entity.getTargetUnit())
                .matterName(entity.getMatterName())
                .approvedAt(entity.getApprovedAt())
                .printedAt(entity.getPrintedAt())
                .validUntil(entity.getValidUntil())
                .verifyCount(entity.getVerifyCount())
                .remark(entity.getRemark())
                .build();
    }

    private String getClientIpAddress(final HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    // ========== 管理后台接口 ==========

    /**
     * 分页查询验证记录（管理后台）
     *
     * @param page 当前页
     * @param pageSize 每页大小
     * @param status 状态筛选
     * @param keyword 关键词搜索（编号/律所/律师）
     * @return 分页结果
     */
    public Map<String, Object> getVerificationList(int page, int pageSize, String status, String keyword) {
        Page<LetterVerification> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<LetterVerification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LetterVerification::getDeleted, false);
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(LetterVerification::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w
                    .like(LetterVerification::getApplicationNo, keyword)
                    .or().like(LetterVerification::getFirmName, keyword)
                    .or().like(LetterVerification::getLawyerNames, keyword)
                    .or().like(LetterVerification::getTargetUnit, keyword)
            );
        }
        
        queryWrapper.orderByDesc(LetterVerification::getCreatedAt);
        
        Page<LetterVerification> result = letterVerificationMapper.selectPage(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", result.getCurrent());
        response.put("pageSize", result.getSize());
        response.put("pages", result.getPages());
        
        return response;
    }

    /**
     * 根据ID获取验证详情（管理后台）
     *
     * @param id 验证记录ID
     * @return 验证记录
     */
    public LetterVerification getVerificationById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ID不能为空");
        }
        
        LetterVerification entity = letterVerificationMapper.selectById(id);
        if (entity == null || entity.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "验证记录不存在");
        }
        
        return entity;
    }

    /**
     * 撤销验证（管理后台，通过记录ID）
     *
     * @param id 验证记录ID
     */
    @Transactional
    public void revokeVerificationById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ID不能为空");
        }

        LetterVerification entity = letterVerificationMapper.selectById(id);
        if (entity == null || entity.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "验证记录不存在");
        }

        entity.setStatus(LetterVerification.STATUS_REVOKED);
        letterVerificationMapper.updateById(entity);
        log.info("撤销函件验证（管理后台）: id={}, applicationNo={}", id, entity.getApplicationNo());
    }

    /**
     * 获取验证统计数据（管理后台）
     *
     * @return 统计数据
     */
    public Map<String, Object> getVerificationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<LetterVerification> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(LetterVerification::getDeleted, false);
        
        long total = letterVerificationMapper.selectCount(baseWrapper);
        
        LambdaQueryWrapper<LetterVerification> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(LetterVerification::getDeleted, false)
                .eq(LetterVerification::getStatus, LetterVerification.STATUS_ACTIVE);
        long active = letterVerificationMapper.selectCount(activeWrapper);
        
        LambdaQueryWrapper<LetterVerification> revokedWrapper = new LambdaQueryWrapper<>();
        revokedWrapper.eq(LetterVerification::getDeleted, false)
                .eq(LetterVerification::getStatus, LetterVerification.STATUS_REVOKED);
        long revoked = letterVerificationMapper.selectCount(revokedWrapper);
        
        LambdaQueryWrapper<LetterVerification> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(LetterVerification::getDeleted, false)
                .eq(LetterVerification::getStatus, LetterVerification.STATUS_ACTIVE)
                .lt(LetterVerification::getValidUntil, LocalDateTime.now());
        long expired = letterVerificationMapper.selectCount(expiredWrapper);
        
        stats.put("total", total);
        stats.put("active", active - expired);
        stats.put("expired", expired);
        stats.put("revoked", revoked);
        
        return stats;
    }
}
