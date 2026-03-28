package com.clientservice.infrastructure.config;

import com.clientservice.common.util.PasswordUtil;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.persistence.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 * 在应用完全启动后初始化默认数据并输出启动成功信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final AdminUserMapper adminUserMapper;
    private final PasswordUtil passwordUtil;

    @Value("${server.port:8081}")
    private int serverPort;

    @Value("${spring.application.name:client-service}")
    private String applicationName;

    @Value("${client-service.system.base-url:http://localhost:8081}")
    private String baseUrl;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 初始化默认管理员用户
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
     * 如果数据库中不存在admin用户，则创建一个默认用户
     */
    private void initDefaultAdminUser() {
        try {
            AdminUser existingUser = adminUserMapper.selectByUsername("admin");
            if (existingUser == null) {
                // 创建默认管理员用户
                AdminUser adminUser = AdminUser.builder()
                        .username("admin")
                        .passwordHash(passwordUtil.encode("admin123"))
                        .realName("系统管理员")
                        .email("admin@example.com")
                        .enabled(true)
                        .failedLoginCount(0)
                        .lockedUntil(null)
                        .build();
                
                adminUserMapper.insert(adminUser);
                log.info("已创建默认管理员用户: username=admin, password=***（请及时修改密码）");
            } else {
                log.debug("默认管理员用户已存在: username=admin");
            }
        } catch (Exception e) {
            log.error("初始化默认管理员用户失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}
