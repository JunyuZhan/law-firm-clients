package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationHistoryDTO;
import com.clientservice.application.dto.PortalNotificationItemDTO;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRecordService {

    private final NotificationRecordMapper notificationRecordMapper;
    private final ClientMatterMapper clientMatterMapper;

    /**
     * 获取通知历史
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param notificationType 通知类型（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 通知历史列表
     */
    public List<NotificationHistoryDTO> getNotificationHistory(
            final String matterId,
            final Long clientId,
            final String notificationType,
            final String status,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final Integer limit) {

        LambdaQueryWrapper<NotificationRecord> queryWrapper = new LambdaQueryWrapper<>();

        if (matterId != null && !matterId.isEmpty()) {
            queryWrapper.eq(NotificationRecord::getMatterId, matterId);
        }

        if (clientId != null) {
            queryWrapper.eq(NotificationRecord::getClientId, clientId);
        }

        if (notificationType != null && !notificationType.isEmpty()) {
            queryWrapper.eq(NotificationRecord::getNotificationType, notificationType);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(NotificationRecord::getStatus, status);
        }

        if (startTime != null) {
            queryWrapper.ge(NotificationRecord::getCreatedAt, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(NotificationRecord::getCreatedAt, endTime);
        }

        queryWrapper.orderByDesc(NotificationRecord::getCreatedAt);

        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<NotificationRecord> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能

        List<NotificationRecord> records = notificationRecordMapper.selectPage(page, queryWrapper).getRecords();

        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取通知统计信息
     *
     * @param matterId 项目ID（可选）
     * @param clientId 客户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     */
    public com.clientservice.application.dto.NotificationStatisticsDTO getNotificationStatistics(
            final String matterId,
            final Long clientId,
            final LocalDateTime startTime,
            final LocalDateTime endTime) {

        LambdaQueryWrapper<NotificationRecord> queryWrapper = new LambdaQueryWrapper<>();

        if (matterId != null && !matterId.isEmpty()) {
            queryWrapper.eq(NotificationRecord::getMatterId, matterId);
        }

        if (clientId != null) {
            queryWrapper.eq(NotificationRecord::getClientId, clientId);
        }

        if (startTime != null) {
            queryWrapper.ge(NotificationRecord::getCreatedAt, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(NotificationRecord::getCreatedAt, endTime);
        }

        List<NotificationRecord> records = notificationRecordMapper.selectList(queryWrapper);

        // 计算统计信息
        long total = records.size();
        long success = records.stream().filter(r -> NotificationRecord.STATUS_SUCCESS.equals(r.getStatus())).count();
        long failed = records.stream().filter(r -> NotificationRecord.STATUS_FAILED.equals(r.getStatus())).count();
        long pending = records.stream().filter(r -> NotificationRecord.STATUS_PENDING.equals(r.getStatus())).count();

        // 按类型统计
        List<NotificationRecord> smsRecords = records.stream()
                .filter(r -> NotificationRecord.TYPE_SMS.equals(r.getNotificationType()))
                .collect(Collectors.toList());
        List<NotificationRecord> wechatRecords = records.stream()
                .filter(r -> NotificationRecord.TYPE_WECHAT.equals(r.getNotificationType()))
                .collect(Collectors.toList());
        List<NotificationRecord> emailRecords = records.stream()
                .filter(r -> NotificationRecord.TYPE_EMAIL.equals(r.getNotificationType()))
                .collect(Collectors.toList());

        com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics smsStats = 
                com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics.builder()
                        .total((long) smsRecords.size())
                        .success(smsRecords.stream().filter(r -> NotificationRecord.STATUS_SUCCESS.equals(r.getStatus())).count())
                        .failed(smsRecords.stream().filter(r -> NotificationRecord.STATUS_FAILED.equals(r.getStatus())).count())
                        .pending(smsRecords.stream().filter(r -> NotificationRecord.STATUS_PENDING.equals(r.getStatus())).count())
                        .build();

        com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics wechatStats = 
                com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics.builder()
                        .total((long) wechatRecords.size())
                        .success(wechatRecords.stream().filter(r -> NotificationRecord.STATUS_SUCCESS.equals(r.getStatus())).count())
                        .failed(wechatRecords.stream().filter(r -> NotificationRecord.STATUS_FAILED.equals(r.getStatus())).count())
                        .pending(wechatRecords.stream().filter(r -> NotificationRecord.STATUS_PENDING.equals(r.getStatus())).count())
                        .build();

        com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics emailStats = 
                com.clientservice.application.dto.NotificationStatisticsDTO.TypeStatistics.builder()
                        .total((long) emailRecords.size())
                        .success(emailRecords.stream().filter(r -> NotificationRecord.STATUS_SUCCESS.equals(r.getStatus())).count())
                        .failed(emailRecords.stream().filter(r -> NotificationRecord.STATUS_FAILED.equals(r.getStatus())).count())
                        .pending(emailRecords.stream().filter(r -> NotificationRecord.STATUS_PENDING.equals(r.getStatus())).count())
                        .build();

        return com.clientservice.application.dto.NotificationStatisticsDTO.builder()
                .total(total)
                .success(success)
                .failed(failed)
                .pending(pending)
                .sms(smsStats)
                .wechat(wechatStats)
                .email(emailStats)
                .build();
    }

    /**
     * 转换为DTO
     */
    private NotificationHistoryDTO convertToDTO(final NotificationRecord record) {
        return NotificationHistoryDTO.builder()
                .id(record.getId())
                .matterId(record.getMatterId())
                .clientId(record.getClientId())
                .notificationType(record.getNotificationType())
                .recipient(record.getRecipient())
                .content(record.getContent())
                .status(record.getStatus())
                .errorMessage(record.getErrorMessage())
                .sentAt(record.getSentAt())
                .createdAt(record.getCreatedAt())
                .retryCount(record.getRetryCount())
                .maxRetries(record.getMaxRetries())
                .nextRetryAt(record.getNextRetryAt())
                .lastRetryAt(record.getLastRetryAt())
                .build();
    }

    /**
     * 获取客户门户通知记录。
     *
     * @param clientId 客户ID
     * @param limit 限制数量
     * @return 门户通知列表
     */
    public List<PortalNotificationItemDTO> getPortalNotifications(final Long clientId, final Integer limit) {
        LambdaQueryWrapper<NotificationRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationRecord::getClientId, clientId)
                .orderByDesc(NotificationRecord::getCreatedAt);

        int limitValue = limit != null && limit > 0 ? Math.min(limit, 100) : 20;
        Page<NotificationRecord> page = new Page<>(1, limitValue);
        page.setSearchCount(false);

        List<NotificationRecord> records = notificationRecordMapper.selectPage(page, queryWrapper).getRecords();

        return records.stream()
                .map(this::convertToPortalDTO)
                .collect(Collectors.toList());
    }

    private PortalNotificationItemDTO convertToPortalDTO(final NotificationRecord record) {
        ClientMatter matter = null;
        if (record.getMatterId() != null && !record.getMatterId().isBlank()) {
            matter = clientMatterMapper.selectById(record.getMatterId());
        }

        String matterName = matter != null ? extractMatterName(matter) : null;
        String title = firstNonBlank(
                matterName != null ? matterName + " 通知" : null,
                record.getNotificationType() + " 通知");

        return PortalNotificationItemDTO.builder()
                .id(record.getId())
                .matterId(record.getMatterId())
                .matterName(matterName)
                .title(title)
                .content(record.getContent())
                .notificationType(record.getNotificationType())
                .status(record.getStatus())
                .recipient(record.getRecipient())
                .sentAt(record.getSentAt())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String extractMatterName(final ClientMatter matter) {
        if (matter == null || matter.getMatterData() == null || matter.getMatterData().isBlank()) {
            return matter != null ? matter.getId() : null;
        }

        try {
            Map<String, Object> matterData = new ObjectMapper().readValue(
                    matter.getMatterData(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object matterName = matterData.get("matterName");
            if (matterName != null && !String.valueOf(matterName).isBlank()) {
                return String.valueOf(matterName).trim();
            }
        } catch (Exception e) {
            log.warn("解析门户通知对应的事项名称失败: matterId={}", matter.getId(), e);
        }
        return matter.getId();
    }

    private String firstNonBlank(final String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
