package com.clientservice.infrastructure.config;

import com.clientservice.application.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置
 * 支持从数据库动态读取 SMTP 配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final SysConfigService sysConfigService;

    @Value("${spring.mail.host:smtp.example.com}")
    private String defaultHost;

    @Value("${spring.mail.port:587}")
    private Integer defaultPort;

    @Value("${spring.mail.username:}")
    private String defaultUsername;

    @Value("${spring.mail.password:}")
    private String defaultPassword;

    /**
     * 创建 JavaMailSender Bean
     * 优先从数据库读取配置，如果数据库没有则使用配置文件默认值
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // 从数据库读取 SMTP 配置（优先），如果没有则使用配置文件默认值
        String host = sysConfigService.getConfigValue("spring.mail.host", defaultHost);
        String portStr = sysConfigService.getConfigValue("spring.mail.port", String.valueOf(defaultPort));
        String username = sysConfigService.getConfigValue("spring.mail.username", defaultUsername);
        String password = sysConfigService.getConfigValue("spring.mail.password", defaultPassword);

        mailSender.setHost(host);
        try {
            mailSender.setPort(Integer.parseInt(portStr));
        } catch (NumberFormatException e) {
            log.warn("SMTP 端口配置无效: {}, 使用默认端口: {}", portStr, defaultPort);
            mailSender.setPort(defaultPort);
        }
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        // 配置邮件属性
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        log.info("邮件服务配置完成: host={}, port={}, username={}", host, mailSender.getPort(), username);

        return mailSender;
    }

    /**
     * 动态创建 JavaMailSender（用于配置更新后重新创建）
     * 注意：此方法需要手动调用，配置更新后需要重新创建 Bean
     */
    public JavaMailSender createDynamicMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // 从数据库读取最新配置
        String host = sysConfigService.getConfigValue("spring.mail.host", defaultHost);
        String portStr = sysConfigService.getConfigValue("spring.mail.port", String.valueOf(defaultPort));
        String username = sysConfigService.getConfigValue("spring.mail.username", defaultUsername);
        String password = sysConfigService.getConfigValue("spring.mail.password", defaultPassword);

        mailSender.setHost(host);
        try {
            mailSender.setPort(Integer.parseInt(portStr));
        } catch (NumberFormatException e) {
            log.warn("SMTP 端口配置无效: {}, 使用默认端口: {}", portStr, defaultPort);
            mailSender.setPort(defaultPort);
        }
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        log.info("动态创建邮件服务: host={}, port={}, username={}", host, mailSender.getPort(), username);

        return mailSender;
    }
}
