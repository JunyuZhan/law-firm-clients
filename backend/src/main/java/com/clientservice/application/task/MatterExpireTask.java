package com.clientservice.application.task;

import com.clientservice.application.service.AccessLogService;
import com.clientservice.application.service.MatterService;
import com.clientservice.application.service.NotificationRetryService;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 项目过期处理定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatterExpireTask {

    private final MatterService matterService;
    private final ClientMatterMapper matterMapper;
    private final AccessLogService accessLogService;
    private final NotificationRetryService notificationRetryService;

    /** 访问日志保留天数（默认90天） */
    @Value("${client-service.access-log.retention-days:90}")
    private int retentionDays;

    /**
     * 处理过期项目
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processExpiredMatters() {
        log.info("开始处理过期项目...");

        try {
            // 查询所有未过期且已到期的项目
            LambdaQueryWrapper<ClientMatter> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ClientMatter::getStatus, ClientMatter.STATUS_ACTIVE)
                    .le(ClientMatter::getExpiresAt, LocalDateTime.now())
                    .isNotNull(ClientMatter::getExpiresAt);

            List<ClientMatter> expiredMatters = matterMapper.selectList(queryWrapper);

            if (expiredMatters.isEmpty()) {
                log.info("没有需要处理的过期项目");
                return;
            }

            log.info("发现{}个过期项目，开始处理...", expiredMatters.size());

            int successCount = 0;
            int failCount = 0;

            for (ClientMatter matter : expiredMatters) {
                try {
                    matterService.expireMatter(matter.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("处理过期项目失败: matterId={}", matter.getId(), e);
                    failCount++;
                }
            }

            log.info("过期项目处理完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("处理过期项目时发生异常", e);
        }
    }

    /**
     * 清理过期访问日志
     * 每周日凌晨3点执行，清理指定天数前的访问日志
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void cleanExpiredAccessLogs() {
        log.info("开始清理过期访问日志...");

        try {
            // 计算截止日期（保留最近N天的数据）
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            // 删除过期访问日志
            int deletedCount = accessLogService.deleteBefore(cutoffDate);
            
            log.info("访问日志清理完成: cutoffDate={}, deletedCount={}, retentionDays={}", 
                    cutoffDate, deletedCount, retentionDays);
        } catch (Exception e) {
            log.error("清理访问日志时发生异常", e);
        }
    }

    /**
     * 重试失败的通知
     * 每30分钟执行一次，重试失败的通知
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void retryFailedNotifications() {
        log.info("开始重试失败的通知...");
        try {
            notificationRetryService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("重试失败通知时发生异常", e);
        }
    }
}
