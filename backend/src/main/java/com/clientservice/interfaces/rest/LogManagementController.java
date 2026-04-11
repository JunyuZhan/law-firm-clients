package com.clientservice.interfaces.rest;

import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.AdminLoginLogService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.clientservice.infrastructure.persistence.mapper.DownloadLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 日志管理控制器
 * 
 * <p>提供登录日志和下载日志的查询、导出功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/logs")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "登录日志和下载日志的查询、导出功能")
public class LogManagementController {

    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminLoginLogService adminLoginLogService;
    private final AdminLoginLogMapper adminLoginLogMapper;
    private final DownloadLogMapper downloadLogMapper;

    /**
     * 获取登录日志列表
     */
    @Operation(summary = "获取登录日志列表", description = "分页查询管理员登录日志")
    @GetMapping("/login")
    public Result<IPage<AdminLoginLog>> getLoginLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        adminAuthorizationService.requireSuperAdmin();
        // 分页参数安全校验
        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, Math.min(100, pageSize));
        IPage<AdminLoginLog> page = adminLoginLogService.getLoginHistory(userId, startTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 导出登录日志为 CSV
     */
    @Operation(summary = "导出登录日志", description = "导出登录日志为 CSV 文件")
    @GetMapping("/login/export")
    public void exportLoginLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        adminAuthorizationService.requireSuperAdmin();
        
        // 查询所有符合条件的日志（不分页）
        LambdaQueryWrapper<AdminLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminLoginLog::getDeleted, false); // 排除已删除的记录
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
        wrapper.last("LIMIT 100000");
        
        List<AdminLoginLog> logs = adminLoginLogMapper.selectList(wrapper);
        
        // 设置响应头
        String filename = "login_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        
        // 写入 CSV（try-with-resources 确保资源释放）
        try (PrintWriter writer = response.getWriter()) {
            // CSV 头部（BOM for Excel）
            writer.write("\uFEFF");
            writer.println("ID,用户名,IP地址,登录时间,是否成功,失败原因");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AdminLoginLog log : logs) {
                writer.printf("%d,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    escapeCsv(log.getUsername()),
                    escapeCsv(log.getIpAddress()),
                    log.getLoginTime() != null ? log.getLoginTime().format(formatter) : "",
                    log.getSuccess() != null && log.getSuccess() ? "是" : "否",
                    escapeCsv(log.getFailureReason() != null ? log.getFailureReason() : "")
                );
            }
            
            writer.flush();
        }
    }

    /**
     * 获取下载日志列表
     */
    @Operation(summary = "获取下载日志列表", description = "分页查询文件下载日志")
    @GetMapping("/download")
    public Result<IPage<DownloadLog>> getDownloadLogs(
            @RequestParam(required = false) String matterId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        adminAuthorizationService.requireSuperAdmin();
        // 分页参数安全校验
        pageNum = Math.max(1, pageNum);
        pageSize = Math.max(1, Math.min(100, pageSize));
        
        Page<DownloadLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getDeleted, false); // 排除已删除的记录
        
        if (matterId != null && !matterId.isEmpty()) {
            wrapper.eq(DownloadLog::getMatterId, matterId);
        }
        if (clientId != null) {
            wrapper.eq(DownloadLog::getClientId, clientId);
        }
        if (startTime != null) {
            wrapper.ge(DownloadLog::getDownloadTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DownloadLog::getDownloadTime, endTime);
        }
        
        wrapper.orderByDesc(DownloadLog::getDownloadTime);
        
        IPage<DownloadLog> result = downloadLogMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 导出下载日志为 CSV
     */
    @Operation(summary = "导出下载日志", description = "导出下载日志为 CSV 文件")
    @GetMapping("/download/export")
    public void exportDownloadLogs(
            @RequestParam(required = false) String matterId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        adminAuthorizationService.requireSuperAdmin();
        
        // 查询所有符合条件的日志（不分页）
        LambdaQueryWrapper<DownloadLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLog::getDeleted, false); // 排除已删除的记录
        if (matterId != null && !matterId.isEmpty()) {
            wrapper.eq(DownloadLog::getMatterId, matterId);
        }
        if (clientId != null) {
            wrapper.eq(DownloadLog::getClientId, clientId);
        }
        if (startTime != null) {
            wrapper.ge(DownloadLog::getDownloadTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DownloadLog::getDownloadTime, endTime);
        }
        wrapper.orderByDesc(DownloadLog::getDownloadTime);
        wrapper.last("LIMIT 100000");
        
        List<DownloadLog> logs = downloadLogMapper.selectList(wrapper);
        
        // 设置响应头
        String filename = "download_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        
        // 写入 CSV（try-with-resources 确保资源释放）
        try (PrintWriter writer = response.getWriter()) {
            // CSV 头部（BOM for Excel）
            writer.write("\uFEFF");
            writer.println("ID,项目ID,客户ID,文件ID,文件名,IP地址,下载时间");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (DownloadLog log : logs) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    escapeCsv(log.getMatterId()),
                    log.getClientId() != null ? log.getClientId().toString() : "",
                    escapeCsv(log.getFileId()),
                    escapeCsv(log.getFileName()),
                    escapeCsv(log.getIpAddress()),
                    log.getDownloadTime() != null ? log.getDownloadTime().format(formatter) : ""
                );
            }
            
            writer.flush();
        }
    }

    /**
     * 转义 CSV 字段中的特殊字符
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
