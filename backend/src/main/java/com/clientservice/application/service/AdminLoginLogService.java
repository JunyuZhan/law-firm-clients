package com.clientservice.application.service;

import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 管理员登录日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginLogService {

    private final AdminLoginLogMapper adminLoginLogMapper;

    /**
     * 记录登录日志
     *
     * @param loginLog 登录日志
     */
    public void recordLogin(AdminLoginLog loginLog) {
        adminLoginLogMapper.insert(loginLog);
    }

    /**
     * 获取登录历史
     *
     * @param userId 用户ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 登录历史列表
     */
    public IPage<AdminLoginLog> getLoginHistory(Long userId, LocalDateTime startTime, LocalDateTime endTime, 
                                                 int pageNum, int pageSize) {
        Page<AdminLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdminLoginLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(AdminLoginLog::getUserId, userId);
        }
        if (startTime != null) {
            wrapper.ge(AdminLoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AdminLoginLog::getLoginTime, endTime);
        }

        wrapper.orderByDesc(AdminLoginLog::getLoginTime);

        return adminLoginLogMapper.selectPage(page, wrapper);
    }
}
