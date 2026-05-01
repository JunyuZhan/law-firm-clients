package com.clientservice.infrastructure.config;

import com.clientservice.common.util.PasswordUtil;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.security.JwtSecretProvider;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 应用启动监听器
 * 在应用完全启动后初始化默认数据并输出启动成功信息
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final String ADMIN_USERNAME = "admin";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JdbcTemplate jdbcTemplate;
    private final PasswordUtil passwordUtil;
    private final JwtSecretProvider jwtSecretProvider;

    @Value("${server.port:8081}")
    private int serverPort;

    @Value("${spring.application.name:client-service}")
    private String applicationName;

    @Value("${client-service.system.base-url:http://localhost:8081}")
    private String baseUrl;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        bootstrapDatabase();
        ensureJwtSecret();
        initDefaultAdminUser();
        
        // 输出启动成功信息（在Spring Boot banner之后）
        System.out.println("");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                            ║");
        System.out.println("║         ✨ 客户服务系统启动成功！                          ║");
        System.out.println("║                                                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║  应用名称: %-50s ║", applicationName));
        System.out.println(String.format("║  服务端口: %-50s ║", serverPort));
        System.out.println(String.format("║  访问地址: %-50s ║", baseUrl));
        System.out.println("║                                                            ║");
        System.out.println("║  快速链接:                                                ║");
        System.out.println(String.format("║    📖 API文档: %-45s ║", baseUrl + "/swagger-ui/index.html"));
        System.out.println(String.format("║    ❤️  健康检查: %-45s ║", baseUrl + "/api/health"));
        System.out.println(String.format("║    📊 Actuator: %-45s ║", baseUrl + "/actuator"));
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("");
        
        // 同时记录到日志
        log.info("客户服务系统启动成功 - 端口: {}, 访问地址: {}", serverPort, baseUrl);
    }

    /**
     * 初始化默认管理员用户
     * 如果数据库中不存在任何管理员用户，则创建一个随机密码的默认用户
     */
    @Transactional
    private void initDefaultAdminUser() {
        try {
            Integer adminCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM admin_user WHERE deleted = false", Integer.class);
            if (adminCount == null || adminCount == 0) {
                String generatedPassword = generateBootstrapPassword();
                AdminUser adminUser = AdminUser.builder()
                        .username(ADMIN_USERNAME)
                        .passwordHash(passwordUtil.encode(generatedPassword))
                        .realName("系统管理员")
                        .email("archive-admin@localhost")
                        .enabled(true)
                        .failedLoginCount(0)
                        .lockedUntil(null)
                        .build();
                
                jdbcTemplate.update(
                        "INSERT INTO admin_user " +
                                "(username, password_hash, real_name, email, enabled, failed_login_count, deleted) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        adminUser.getUsername(),
                        adminUser.getPasswordHash(),
                        adminUser.getRealName(),
                        adminUser.getEmail(),
                        adminUser.getEnabled(),
                        adminUser.getFailedLoginCount(),
                        Boolean.FALSE);
                log.warn("已自动创建管理员账号，请尽快登录后修改密码: username={}, password={}",
                        ADMIN_USERNAME, generatedPassword);
            } else {
                log.debug("管理员账号已存在，跳过自动创建");
            }
        } catch (Exception e) {
            log.error("初始化默认管理员用户失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    private void bootstrapDatabase() {
        try {
            if (tableExists("admin_user") && tableExists("sys_config")) {
                log.debug("数据库基础表已存在，跳过结构初始化");
                return;
            }
            log.info("检测到数据库未初始化，开始执行内置建表脚本");
            runSqlScript("bootstrap/01-schema.sql");
            runSqlScript("bootstrap/02-seed.sql");
            log.info("数据库内置初始化完成");
        } catch (Exception e) {
            log.error("执行数据库内置初始化失败", e);
            throw new IllegalStateException("数据库初始化失败，应用无法继续启动", e);
        }
    }

    private void ensureJwtSecret() {
        try {
            String configValue = jdbcTemplate.query(
                    "SELECT config_value FROM sys_config WHERE config_key = ? AND deleted = false LIMIT 1",
                    ps -> ps.setString(1, JwtSecretProvider.JWT_SECRET_CONFIG_KEY),
                    rs -> rs.next() ? rs.getString(1) : null);
            if (!StringUtils.hasText(configValue)) {
                String generatedSecret = generateTokenLikeSecret(48);
                int updated = jdbcTemplate.update(
                        "UPDATE sys_config SET config_value = ?, config_type = 'STRING', description = ?, " +
                                "deleted = false, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?",
                        generatedSecret,
                        "JWT signing secret generated automatically for container bootstrap",
                        JwtSecretProvider.JWT_SECRET_CONFIG_KEY);
                if (updated == 0) {
                    jdbcTemplate.update(
                            "INSERT INTO sys_config (config_key, config_value, config_type, description, deleted) " +
                                    "VALUES (?, ?, 'STRING', ?, false)",
                            JwtSecretProvider.JWT_SECRET_CONFIG_KEY,
                            generatedSecret,
                            "JWT signing secret generated automatically for container bootstrap");
                }
                jwtSecretProvider.refresh();
                log.info("已自动生成并持久化 JWT 密钥到 sys_config");
            } else {
                jwtSecretProvider.refresh();
                log.debug("JWT 密钥已存在，跳过自动生成");
            }
        } catch (Exception e) {
            log.error("初始化 JWT 密钥失败", e);
            throw new IllegalStateException("JWT 密钥初始化失败，应用无法继续启动", e);
        }
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((Connection connection) -> {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet resultSet = metadata.getTables(null, "public", tableName, null)) {
                return resultSet.next();
            }
        }));
    }

    private void runSqlScript(String classpathLocation) throws SQLException {
        Resource resource = new ClassPathResource(classpathLocation);
        jdbcTemplate.execute((Connection connection) -> {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(resource, StandardCharsets.UTF_8));
            return null;
        });
    }

    private String generateBootstrapPassword() {
        String password;
        do {
            password = generateTokenLikeSecret(12);
        } while (!passwordUtil.isValidPassword(password));
        return password;
    }

    private String generateTokenLikeSecret(int rawBytesLength) {
        byte[] bytes = new byte[rawBytesLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
