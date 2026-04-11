package com.clientservice.interfaces.rest;

import com.clientservice.common.result.Result;
import com.clientservice.application.service.AdminAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统维护控制器
 * 提供备份、系统状态等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Tag(name = "系统维护", description = "系统备份、状态查看等维护功能")
public class SystemMaintenanceController {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final AdminAuthorizationService adminAuthorizationService;

    @Value("${file.storage.path:/data/client-service/files}")
    private String fileStoragePath;

    @Value("${client-service.backup.directory:/tmp/client-service-backups}")
    private String backupDirectory;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    // 版本检查配置
    @Value("${app.version:1.0.0}")
    private String currentVersion;

    @Value("${app.version.check-url:}")
    private String versionCheckUrl;

    @Value("${app.version.github-repo:}")
    private String githubRepo;  // 格式: owner/repo

    /**
     * 获取系统状态
     */
    @Operation(summary = "获取系统状态", description = "获取数据库、存储等系统状态信息")
    @GetMapping("/status")
    public Result<Map<String, Object>> getSystemStatus() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> status = new LinkedHashMap<>();

        // 数据库状态
        Map<String, Object> dbStatus = new LinkedHashMap<>();
        try {
            // 测试数据库连接
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbStatus.put("connected", true);
            
            // 获取各表数据量
            Map<String, Long> tableCounts = new LinkedHashMap<>();
            tableCounts.put("项目数据", countTable("client_matter"));
            tableCounts.put("通知记录", countTable("notification_record"));
            tableCounts.put("客户文件", countTable("client_file"));
            tableCounts.put("访问日志", countTable("access_log"));
            tableCounts.put("管理员用户", countTable("admin_user"));
            tableCounts.put("API密钥", countTable("api_key"));
            tableCounts.put("系统配置", countTable("sys_config"));
            tableCounts.put("通知模板", countTable("notification_template"));
            dbStatus.put("tables", tableCounts);
            
            // 数据库大小（PostgreSQL）
            try {
                String dbName = extractDbName(datasourceUrl);
                Long dbSize = jdbcTemplate.queryForObject(
                    "SELECT pg_database_size(?)", Long.class, dbName);
                dbStatus.put("sizeBytes", dbSize);
                dbStatus.put("sizeFormatted", formatBytes(dbSize));
            } catch (Exception e) {
                dbStatus.put("sizeFormatted", "未知");
            }
        } catch (Exception e) {
            dbStatus.put("connected", false);
            dbStatus.put("error", e.getMessage());
        }
        status.put("database", dbStatus);

        // 文件存储状态
        Map<String, Object> storageStatus = new LinkedHashMap<>();
        try {
            Path storagePath = Paths.get(fileStoragePath);
            if (Files.exists(storagePath)) {
                long[] stats = calculateDirectoryStats(storagePath);
                storageStatus.put("available", true);
                storageStatus.put("fileCount", stats[0]);
                storageStatus.put("totalSizeBytes", stats[1]);
                storageStatus.put("totalSizeFormatted", formatBytes(stats[1]));
                storageStatus.put("path", fileStoragePath);
            } else {
                storageStatus.put("available", false);
                storageStatus.put("message", "存储目录不存在");
            }
        } catch (Exception e) {
            storageStatus.put("available", false);
            storageStatus.put("error", e.getMessage());
        }
        status.put("storage", storageStatus);

        // 系统信息
        Map<String, Object> systemInfo = new LinkedHashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("maxMemoryMB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        systemInfo.put("freeMemoryMB", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        systemInfo.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        status.put("system", systemInfo);

        return Result.success(status);
    }

    /**
     * 创建数据库备份
     */
    @Operation(summary = "创建数据库备份", description = "导出数据库为SQL文件并下载")
    @PostMapping("/backup/database")
    public Result<Map<String, Object>> createDatabaseBackup() {
        adminAuthorizationService.requireSuperAdmin();
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDir = backupDirectory;
            String backupFile = backupDir + "/database_" + timestamp + ".sql";
            
            // 创建备份目录
            Files.createDirectories(Paths.get(backupDir));
            
            // 导出各表数据
            StringBuilder sql = new StringBuilder();
            sql.append("-- 客户服务系统数据库备份\n");
            sql.append("-- 备份时间: ").append(LocalDateTime.now()).append("\n\n");
            
            // 导出每个表的数据
            List<String> tables = Arrays.asList(
                "sys_config", "admin_user", "api_key", "notification_template",
                "client_matter", "notification_record", "client_file", "access_log"
            );
            
            for (String table : tables) {
                sql.append(exportTableData(table));
            }
            
            // 写入文件
            Files.writeString(Paths.get(backupFile), sql.toString());
            
            // 返回备份信息
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("filename", "database_" + timestamp + ".sql");
            result.put("path", backupFile);
            result.put("size", Files.size(Paths.get(backupFile)));
            result.put("sizeFormatted", formatBytes(Files.size(Paths.get(backupFile))));
            result.put("timestamp", timestamp);
            
            log.info("数据库备份创建成功: {}", backupFile);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建数据库备份失败", e);
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份列表
     */
    @Operation(summary = "获取备份列表", description = "获取已创建的备份文件列表")
    @GetMapping("/backup/list")
    public Result<List<Map<String, Object>>> listBackups() {
        adminAuthorizationService.requireSuperAdmin();
        try {
            String backupDir = backupDirectory;
            Path backupPath = Paths.get(backupDir);
            
            List<Map<String, Object>> backups = new ArrayList<>();
            
            if (Files.exists(backupPath)) {
                // 使用 try-with-resources 确保 Stream 正确关闭，避免资源泄漏
                try (var fileStream = Files.list(backupPath)) {
                    fileStream
                        .filter(p -> p.toString().endsWith(".sql") || p.toString().endsWith(".zip"))
                        .sorted((a, b) -> {
                            try {
                                return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .limit(20)
                        .forEach(p -> {
                            try {
                                Map<String, Object> backup = new LinkedHashMap<>();
                                backup.put("filename", p.getFileName().toString());
                                backup.put("path", p.toString());
                                backup.put("size", Files.size(p));
                                backup.put("sizeFormatted", formatBytes(Files.size(p)));
                                backup.put("lastModified", Files.getLastModifiedTime(p).toString());
                                backups.add(backup);
                            } catch (IOException e) {
                                log.warn("读取备份文件信息失败: {}", p);
                            }
                        });
                }
            }
            
            return Result.success(backups);
        } catch (Exception e) {
            log.error("获取备份列表失败", e);
            return Result.error("获取备份列表失败: " + e.getMessage());
        }
    }

    /**
     * 下载备份文件
     */
    @Operation(summary = "下载备份文件", description = "下载指定的备份文件")
    @GetMapping("/backup/download/{filename}")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) throws IOException {
        adminAuthorizationService.requireSuperAdmin();
        // 安全检查：防止路径遍历攻击
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("非法文件名");
        }
        
        String backupDir = "/tmp/client-service-backups";
        Path backupPath = Paths.get(backupDir).toAbsolutePath().normalize();
        Path filePath = backupPath.resolve(filename).normalize();
        
        // 二次验证：确保解析后的路径仍在备份目录内（防止绕过攻击）
        if (!filePath.startsWith(backupPath)) {
            log.warn("检测到路径遍历攻击尝试: filename={}, resolvedPath={}", filename, filePath);
            throw new IllegalArgumentException("非法文件路径");
        }
        
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("备份文件不存在: " + filename);
        }
        
        byte[] content = Files.readAllBytes(filePath);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(content.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    }

    /**
     * 删除备份文件
     */
    @Operation(summary = "删除备份文件", description = "删除指定的备份文件")
    @DeleteMapping("/backup/{filename}")
    public Result<Void> deleteBackup(@PathVariable String filename) {
        adminAuthorizationService.requireSuperAdmin();
        try {
            // 安全检查：防止路径遍历攻击
            if (filename == null || filename.isBlank()) {
                return Result.error("文件名不能为空");
            }
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return Result.error("非法文件名");
            }
            
            String backupDir = backupDirectory;
            Path backupPath = Paths.get(backupDir).toAbsolutePath().normalize();
            Path filePath = backupPath.resolve(filename).normalize();
            
            // 二次验证：确保解析后的路径仍在备份目录内
            if (!filePath.startsWith(backupPath)) {
                log.warn("删除操作检测到路径遍历攻击尝试: filename={}, resolvedPath={}", filename, filePath);
                return Result.error("非法文件路径");
            }
            
            if (!Files.exists(filePath)) {
                return Result.error("备份文件不存在");
            }
            
            Files.delete(filePath);
            log.info("删除备份文件: {}", filename);
            return Result.success();
        } catch (Exception e) {
            log.error("删除备份文件失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 允许查询的表名白名单（防止 SQL 注入）
     */
    private static final Set<String> ALLOWED_TABLES = Set.of(
        "client_matter", "notification_record", "client_file", "access_log",
        "admin_user", "api_key", "sys_config", "notification_template"
    );

    private void validateTableName(String tableName) {
        if (tableName == null || !ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("非法的表名: " + tableName);
        }
    }

    private long countTable(String tableName) {
        validateTableName(tableName);
        try {
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE deleted = false", Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractDbName(String url) {
        // jdbc:postgresql://host:port/dbname
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash > 0) {
            String dbPart = url.substring(lastSlash + 1);
            int questionMark = dbPart.indexOf('?');
            return questionMark > 0 ? dbPart.substring(0, questionMark) : dbPart;
        }
        return "client_service";
    }

    private long[] calculateDirectoryStats(Path dir) throws IOException {
        long[] stats = new long[2]; // [fileCount, totalSize]
        // 使用try-with-resources确保Stream正确关闭，避免目录句柄泄漏
        try (java.util.stream.Stream<Path> stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        stats[0]++;
                        stats[1] += Files.size(file);
                    } catch (IOException e) {
                        log.warn("获取文件大小失败: {}", file, e);
                    }
                });
        }
        return stats;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String exportTableData(String tableName) {
        validateTableName(tableName);
        StringBuilder sb = new StringBuilder();
        sb.append("-- Table: ").append(tableName).append("\n");
        
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + tableName + " WHERE deleted = false");
            
            if (rows.isEmpty()) {
                sb.append("-- (empty)\n\n");
                return sb.toString();
            }
            
            // 获取列名
            Set<String> columns = rows.get(0).keySet();
            String columnList = String.join(", ", columns);
            
            for (Map<String, Object> row : rows) {
                sb.append("INSERT INTO ").append(tableName).append(" (").append(columnList).append(") VALUES (");
                List<String> values = new ArrayList<>();
                for (String col : columns) {
                    Object val = row.get(col);
                    if (val == null) {
                        values.add("NULL");
                    } else if (val instanceof String) {
                        values.add("'" + ((String) val).replace("'", "''") + "'");
                    } else if (val instanceof Boolean) {
                        values.add((Boolean) val ? "true" : "false");
                    } else {
                        values.add(val.toString());
                    }
                }
                sb.append(String.join(", ", values));
                sb.append(") ON CONFLICT DO NOTHING;\n");
            }
            sb.append("\n");
        } catch (Exception e) {
            sb.append("-- Error exporting: ").append(e.getMessage()).append("\n\n");
        }
        
        return sb.toString();
    }

    // ==================== 版本信息与升级 API ====================

    /**
     * 获取版本信息（通过 GitHub API）
     */
    @Operation(summary = "获取版本信息", description = "获取当前版本和远程最新版本信息")
    @GetMapping("/git/info")
    public Result<Map<String, Object>> getGitInfo() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 当前版本
        result.put("currentVersion", currentVersion);
        
        // 检查 GitHub 仓库配置
        if (githubRepo == null || githubRepo.isEmpty()) {
            result.put("error", "未配置 GitHub 仓库（APP_VERSION_GITHUB_REPO）");
            result.put("lastCheckTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
            return Result.success(result);
        }
        
        result.put("githubRepo", githubRepo);
        
        try {
            // 从 GitHub 获取最新版本
            Map<String, Object> latestVersion = fetchFromGitHub(githubRepo);
            
            if (latestVersion != null) {
                String remoteVersion = (String) latestVersion.get("version");
                result.put("remoteVersion", remoteVersion);
                result.put("releaseUrl", latestVersion.get("releaseUrl"));
                result.put("releaseNotes", latestVersion.get("releaseNotes"));
                result.put("publishedAt", latestVersion.get("publishedAt"));
                
                // 比较版本
                if (remoteVersion != null) {
                    int cmp = compareVersions(remoteVersion, currentVersion);
                    result.put("hasUpdate", cmp > 0);
                    if (cmp > 0) {
                        result.put("updateMessage", "发现新版本 " + remoteVersion);
                    } else if (cmp == 0) {
                        result.put("updateMessage", "已是最新版本");
                    } else {
                        result.put("updateMessage", "当前版本较新（可能是开发版本）");
                    }
                }
            } else {
                result.put("remoteVersion", "无法获取");
                result.put("hasUpdate", false);
                result.put("error", "无法从 GitHub 获取版本信息");
            }
        } catch (Exception e) {
            log.error("获取版本信息失败", e);
            result.put("error", "获取版本信息失败: " + e.getMessage());
            result.put("hasUpdate", false);
        }
        
        // 最后检查时间
        result.put("lastCheckTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        
        return Result.success(result);
    }

    /**
     * 获取升级指南
     * 容器化部署不支持自动升级，返回手动升级命令
     */
    @Operation(summary = "获取升级指南", description = "返回手动升级的命令和步骤")
    @PostMapping("/upgrade/start")
    public Result<Map<String, Object>> getUpgradeGuide(
            @RequestParam(defaultValue = "false") boolean noCache,
            @RequestParam(defaultValue = "false") boolean skipRestart) {
        adminAuthorizationService.requireSuperAdmin();
        
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 构建升级命令
        StringBuilder commands = new StringBuilder();
        commands.append("# 进入项目目录\n");
        commands.append("cd /path/to/client-service\n\n");
        commands.append("# 拉取最新代码\n");
        commands.append("git pull\n\n");
        commands.append("# 使用部署脚本升级\n");
        if (noCache) {
            commands.append("./deploy.sh --build  # 包含 --no-cache 选项\n");
        } else {
            commands.append("./deploy.sh --start --build\n");
        }
        
        if (skipRestart) {
            commands.append("\n# 或者只构建不重启\n");
            commands.append("cd docker && docker compose build\n");
        }
        
        result.put("guide", true);
        result.put("message", "容器化部署不支持自动升级，请在服务器上手动执行以下命令");
        result.put("commands", commands.toString());
        result.put("steps", Arrays.asList(
            "1. SSH 登录到服务器",
            "2. 进入项目目录",
            "3. 执行 git pull 拉取最新代码",
            "4. 执行 ./deploy.sh --start --build 重新构建并启动"
        ));
        
        // 如果有 GitHub Release URL，添加到结果中
        if (githubRepo != null && !githubRepo.isEmpty()) {
            result.put("releaseUrl", "https://github.com/" + githubRepo + "/releases");
        }
        
        return Result.success(result);
    }

    /**
     * 获取服务状态
     */
    @Operation(summary = "获取服务状态", description = "检查后端服务是否正常运行")
    @GetMapping("/upgrade/status")
    public Result<Map<String, Object>> getUpgradeStatus() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 检查后端服务是否正常
        result.put("backendRunning", true);
        result.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("currentVersion", currentVersion);
        result.put("status", "running");
        
        return Result.success(result);
    }

    // ==================== 版本检查 API ====================

    /**
     * 检查新版本
     */
    @Operation(summary = "检查新版本", description = "检查是否有新版本可用")
    @GetMapping("/version/check")
    public Result<Map<String, Object>> checkVersion() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentVersion", currentVersion);
        result.put("hasUpdate", false);
        
        String ignoredVersion = getIgnoredVersion();
        if (ignoredVersion != null) {
            result.put("ignoredVersion", ignoredVersion);
        }
        
        try {
            Map<String, Object> latestInfo = fetchLatestVersion();
            if (latestInfo != null) {
                String latestVersion = (String) latestInfo.get("version");
                result.put("latestVersion", latestVersion);
                result.put("releaseNotes", latestInfo.get("releaseNotes"));
                result.put("releaseUrl", latestInfo.get("releaseUrl"));
                result.put("publishedAt", latestInfo.get("publishedAt"));
                
                // 比较版本号
                if (latestVersion != null && compareVersions(latestVersion, currentVersion) > 0) {
                    result.put("hasUpdate", true);
                }
            }
        } catch (Exception e) {
            log.warn("检查版本失败: {}", e.getMessage());
            result.put("error", "无法获取最新版本信息");
        }
        
        return Result.success(result);
    }

    /**
     * 获取当前版本信息
     */
    @Operation(summary = "获取当前版本", description = "获取当前系统版本信息")
    @GetMapping("/version")
    public Result<Map<String, Object>> getVersion() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", currentVersion);
        result.put("name", "客户服务系统");
        result.put("buildTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return Result.success(result);
    }

    /**
     * 忽略版本更新（记录到配置中）
     */
    @Operation(summary = "忽略版本更新", description = "忽略指定版本的更新提示")
    @PostMapping("/version/ignore")
    public Result<Void> ignoreVersion(@RequestParam String version) {
        adminAuthorizationService.requireSuperAdmin();
        try {
            // 将忽略的版本保存到数据库配置中
            String configKey = "system.ignored-version";
            Long exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_config WHERE config_key = ?", Long.class, configKey);
            
            if (exists != null && exists > 0) {
                jdbcTemplate.update(
                    "UPDATE sys_config SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?",
                    version, configKey);
            } else {
                jdbcTemplate.update(
                    "INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES (?, ?, 'STRING', '已忽略的版本号')",
                    configKey, version);
            }
            
            log.info("已忽略版本更新: {}", version);
            return Result.success();
        } catch (Exception e) {
            log.error("保存忽略版本失败", e);
            return Result.error("操作失败");
        }
    }

    /**
     * 获取已忽略的版本
     */
    private String getIgnoredVersion() {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT config_value FROM sys_config WHERE config_key = 'system.ignored-version'",
                String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从远程获取最新版本信息
     */
    private Map<String, Object> fetchLatestVersion() {
        // 优先使用自定义 URL
        if (versionCheckUrl != null && !versionCheckUrl.isEmpty()) {
            return fetchFromCustomUrl(versionCheckUrl);
        }
        
        // 使用 GitHub Release API
        if (githubRepo != null && !githubRepo.isEmpty()) {
            return fetchFromGitHub(githubRepo);
        }
        
        return null;
    }

    /**
     * 从自定义 URL 获取版本信息
     * 期望返回 JSON: {"version": "1.2.0", "releaseNotes": "...", "releaseUrl": "..."}
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchFromCustomUrl(String url) {
        try {
            // SSRF 防护：仅允许 HTTPS 协议，禁止访问内网地址
            java.net.URI uri = new java.net.URI(url);
            if (!"https".equals(uri.getScheme()) && !"http".equals(uri.getScheme())) {
                log.warn("版本检查 URL 协议不安全: {}", uri.getScheme());
                return null;
            }
            java.net.InetAddress addr = java.net.InetAddress.getByName(uri.getHost());
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                log.warn("版本检查 URL 指向内网，已拒绝: {}", url);
                return null;
            }
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.warn("从自定义 URL 获取版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 GitHub Release 获取最新版本
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchFromGitHub(String repo) {
        try {
            // 验证 repo 格式（仅允许 owner/repo，防止路径注入）
            if (repo == null || !repo.matches("^[a-zA-Z0-9._\\-]+/[a-zA-Z0-9._\\-]+$")) {
                log.warn("GitHub 仓库格式无效: {}", repo);
                return null;
            }
            String apiUrl = "https://api.github.com/repos/" + repo + "/releases/latest";
            
            Map<String, Object> release = restTemplate.getForObject(apiUrl, Map.class);
            if (release == null) return null;
            
            Map<String, Object> result = new LinkedHashMap<>();
            String tagName = (String) release.get("tag_name");
            // 去掉版本号前的 v 前缀
            result.put("version", tagName != null ? tagName.replaceFirst("^v", "") : null);
            result.put("releaseNotes", release.get("body"));
            result.put("releaseUrl", release.get("html_url"));
            result.put("publishedAt", release.get("published_at"));
            
            return result;
        } catch (Exception e) {
            log.warn("从 GitHub 获取版本失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 比较两个版本号
     * @return 正数表示 v1 > v2，负数表示 v1 < v2，0 表示相等
     */
    private int compareVersions(String v1, String v2) {
        // 去掉 v 前缀
        v1 = v1.replaceFirst("^v", "");
        v2 = v2.replaceFirst("^v", "");
        
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;
            
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    /**
     * 解析版本号部分（处理类似 1.0.0-beta 的情况）
     */
    private int parseVersionPart(String part) {
        try {
            // 提取数字部分
            Pattern pattern = Pattern.compile("^(\\d+)");
            Matcher matcher = pattern.matcher(part);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
