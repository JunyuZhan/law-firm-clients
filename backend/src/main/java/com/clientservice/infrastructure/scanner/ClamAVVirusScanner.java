package com.clientservice.infrastructure.scanner;

import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collection;

/**
 * ClamAV病毒扫描器
 * 注意：需要clamav-client依赖，如果依赖不可用，此组件不会被加载
 */
@Slf4j
@Component
@ConditionalOnClass(name = "xyz.capybara.clamav.ClamavClient")
public class ClamAVVirusScanner implements VirusScanner {

    @Value("${client-service.file.virus-scan.enabled:false}")
    private boolean enabled;

    @Value("${client-service.file.virus-scan.clamav.host:localhost}")
    private String host;

    @Value("${client-service.file.virus-scan.clamav.port:3310}")
    private int port;

    @Value("${client-service.file.virus-scan.clamav.timeout:5000}")
    private int timeout;

    private ClamavClient clamavClient;
    private volatile boolean initializationFailed = false;

    /**
     * 获取ClamAV客户端（懒加载）
     */
    private ClamavClient getClamavClient() throws Exception {
        if (initializationFailed) {
            throw new IllegalStateException("ClamAV客户端初始化已失败，无法使用");
        }
        
        if (clamavClient == null) {
            synchronized (this) {
                if (clamavClient == null) {
                    try {
                        clamavClient = new ClamavClient(host, port);
                        log.info("ClamAV客户端初始化成功: host={}, port={}", host, port);
                    } catch (Exception e) {
                        initializationFailed = true;
                        log.error("ClamAV客户端初始化失败: host={}, port={}", host, port, e);
                        throw new IllegalStateException("ClamAV客户端初始化失败: " + e.getMessage(), e);
                    }
                }
            }
        }
        return clamavClient;
    }

    @Override
    public VirusScanResult scanFile(MultipartFile file) throws Exception {
        if (!isEnabled()) {
            log.debug("病毒扫描未启用，跳过扫描");
            return VirusScanResult.safe("ClamAV (disabled)");
        }

        try {
            ClamavClient client = getClamavClient();
            
            try (InputStream inputStream = file.getInputStream()) {
                // 执行扫描（使用INSTREAM命令）
                ScanResult result = client.scan(inputStream);
                
                // ScanResult是Kotlin sealed class，有两个子类：
                // - ScanResult.OK: 扫描通过
                // - ScanResult.VirusFound: 发现病毒
                if (result instanceof ScanResult.OK) {
                    log.debug("文件扫描通过: fileName={}", file.getOriginalFilename());
                    return VirusScanResult.safe("ClamAV");
                } else if (result instanceof ScanResult.VirusFound) {
                    ScanResult.VirusFound virusFound = (ScanResult.VirusFound) result;
                    // foundViruses是一个Map<String, Collection<String>>
                    // Key: 文件路径（对于INSTREAM扫描，通常是"stream"）
                    // Value: 病毒名称列表
                    String virusName = virusFound.getFoundViruses().values().stream()
                            .flatMap(Collection::stream)
                            .findFirst()
                            .orElse("Unknown");
                    log.warn("发现病毒: fileName={}, virus={}", file.getOriginalFilename(), virusName);
                    return VirusScanResult.infected("ClamAV", virusName);
                } else {
                    // 未知结果类型
                    log.error("未知的扫描结果类型: fileName={}, resultType={}", 
                            file.getOriginalFilename(), result.getClass().getName());
                    return VirusScanResult.error("ClamAV", "未知的扫描结果类型");
                }
            }
        } catch (Exception e) {
            log.error("病毒扫描失败: fileName={}", file.getOriginalFilename(), e);
            // 扫描失败时，根据配置决定是否允许上传
            // 这里选择返回错误，不允许上传（更安全）
            return VirusScanResult.error("ClamAV", e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
