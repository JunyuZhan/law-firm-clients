package com.clientservice.infrastructure.scanner;

import org.springframework.web.multipart.MultipartFile;

/**
 * 病毒扫描器接口
 */
public interface VirusScanner {

    /**
     * 扫描文件
     *
     * @param file 文件
     * @return 扫描结果
     * @throws Exception 扫描异常
     */
    VirusScanResult scanFile(MultipartFile file) throws Exception;

    /**
     * 是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();
}
