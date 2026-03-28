package com.clientservice.infrastructure.scanner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 病毒扫描器工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VirusScannerFactory {

    @Autowired(required = false)
    private ClamAVVirusScanner clamAVVirusScanner;
    private final NoOpVirusScanner noOpVirusScanner;

    @Value("${client-service.file.virus-scan.enabled:false}")
    private boolean enabled;

    @Value("${client-service.file.virus-scan.type:clamav}")
    private String scannerType;

    /**
     * 获取病毒扫描器
     *
     * @return 病毒扫描器实例
     */
    public VirusScanner getVirusScanner() {
        if (!enabled) {
            log.debug("病毒扫描未启用，使用空操作扫描器");
            return noOpVirusScanner;
        }

        switch (scannerType.toUpperCase()) {
            case "CLAMAV":
                if (clamAVVirusScanner != null && clamAVVirusScanner.isEnabled()) {
                    log.info("使用ClamAV病毒扫描器");
                    return clamAVVirusScanner;
                } else {
                    if (clamAVVirusScanner == null) {
                        log.warn("ClamAV扫描器不可用（依赖缺失），使用空操作扫描器");
                    } else {
                        log.warn("ClamAV扫描器未启用，使用空操作扫描器");
                    }
                    return noOpVirusScanner;
                }
            case "NONE":
            default:
                log.debug("使用空操作扫描器");
                return noOpVirusScanner;
        }
    }
}
