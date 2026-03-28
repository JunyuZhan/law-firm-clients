package com.clientservice.infrastructure.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 空操作病毒扫描器（不执行扫描，直接返回安全）
 * 当病毒扫描未启用时使用
 */
@Slf4j
@Component
public class NoOpVirusScanner implements VirusScanner {

    @Override
    public VirusScanResult scanFile(MultipartFile file) throws Exception {
        log.debug("使用空操作扫描器，跳过病毒扫描");
        return VirusScanResult.safe("NoOp");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
