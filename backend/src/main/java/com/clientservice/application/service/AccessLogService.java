package com.clientservice.application.service;

import com.clientservice.application.dto.AccessLogDTO;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.infrastructure.persistence.mapper.AccessLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 访问日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogMapper accessLogMapper;
    private final CallbackService callbackService;

    /**
     * 记录访问日志
     *
     * @param matterId 项目ID
     * @param clientId 客户ID
     * @param accessToken 访问令牌
     * @param request HTTP请求
     */
    @Transactional
    public void recordAccess(
            final String matterId,
            final Long clientId,
            final String accessToken,
            final HttpServletRequest request) {

        AccessLog accessLog = AccessLog.builder()
                .matterId(matterId)
                .clientId(clientId)
                .accessToken(accessToken)
                .ipAddress(getClientIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .accessTime(LocalDateTime.now())
                .build();

        accessLogMapper.insert(accessLog);
        log.debug("记录访问日志: matterId={}, ipAddress={}", matterId, accessLog.getIpAddress());

        // 异步回调给管理系统（失败不影响主流程）
        try {
            callbackService.callbackAccessLog(accessLog);
        } catch (Exception e) {
            // 回调失败不影响主流程
            log.warn("访问日志回调失败: matterId={}, clientId={}", matterId, clientId, e);
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取访问历史
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 访问日志列表
     */
    public List<AccessLogDTO> getAccessHistory(
            final String matterId,
            final Long clientId,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final Integer limit) {

        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        
        if (matterId != null && !matterId.isEmpty()) {
            queryWrapper.eq(AccessLog::getMatterId, matterId);
        }
        
        if (clientId != null) {
            queryWrapper.eq(AccessLog::getClientId, clientId);
        }
        
        if (startTime != null) {
            queryWrapper.ge(AccessLog::getAccessTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(AccessLog::getAccessTime, endTime);
        }
        
        queryWrapper.orderByDesc(AccessLog::getAccessTime);
        
        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<AccessLog> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能

        List<AccessLog> logs = accessLogMapper.selectPage(page, queryWrapper).getRecords();
        
        return logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取访问统计
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 访问统计信息
     */
    public AccessStatistics getAccessStatistics(
            final String matterId,
            final Long clientId,
            final LocalDateTime startTime,
            final LocalDateTime endTime) {

        LambdaQueryWrapper<AccessLog> queryWrapper = new LambdaQueryWrapper<>();
        
        if (matterId != null && !matterId.isEmpty()) {
            queryWrapper.eq(AccessLog::getMatterId, matterId);
        }
        
        if (clientId != null) {
            queryWrapper.eq(AccessLog::getClientId, clientId);
        }
        
        if (startTime != null) {
            queryWrapper.ge(AccessLog::getAccessTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(AccessLog::getAccessTime, endTime);
        }

        long totalCount = accessLogMapper.selectCount(queryWrapper);

        // 统计唯一IP数量：先查询所有IP，再统计去重后的数量
        List<AccessLog> logs = accessLogMapper.selectList(queryWrapper);
        long uniqueIpCount = logs.stream()
                .map(AccessLog::getIpAddress)
                .filter(ip -> ip != null && !ip.isEmpty())
                .distinct()
                .count();

        return AccessStatistics.builder()
                .totalCount(totalCount)
                .uniqueIpCount(uniqueIpCount)
                .build();
    }

    /**
     * 转换为DTO
     */
    private AccessLogDTO convertToDTO(final AccessLog log) {
        return AccessLogDTO.builder()
                .id(log.getId())
                .matterId(log.getMatterId())
                .clientId(log.getClientId())
                .accessToken(log.getAccessToken())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .accessTime(log.getAccessTime())
                .createdAt(log.getCreatedAt())
                .build();
    }

    /**
     * 删除指定时间之前的访问日志
     *
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    @Transactional
    public int deleteBefore(final LocalDateTime cutoffDate) {
        if (cutoffDate == null) {
            log.warn("截止日期为空，跳过删除操作");
            return 0;
        }

        int deletedCount = accessLogMapper.deleteBefore(cutoffDate);
        log.info("删除过期访问日志完成: cutoffDate={}, deletedCount={}", cutoffDate, deletedCount);
        return deletedCount;
    }

    /**
     * 访问统计信息
     */
    @Data
    @Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AccessStatistics {
        /** 总访问次数 */
        private Long totalCount;
        
        /** 唯一IP数量 */
        private Long uniqueIpCount;
    }
}
