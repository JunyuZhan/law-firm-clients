package com.clientservice.infrastructure.scanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 病毒扫描结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirusScanResult {

    /** 是否安全 */
    private boolean safe;

    /** 是否发现病毒 */
    private boolean infected;

    /** 病毒名称（如果发现病毒） */
    private String virusName;

    /** 扫描消息 */
    private String message;

    /** 扫描器名称 */
    private String scannerName;

    /**
     * 创建安全结果
     */
    public static VirusScanResult safe(String scannerName) {
        return VirusScanResult.builder()
                .safe(true)
                .infected(false)
                .message("文件安全")
                .scannerName(scannerName)
                .build();
    }

    /**
     * 创建发现病毒的结果
     */
    public static VirusScanResult infected(String scannerName, String virusName) {
        return VirusScanResult.builder()
                .safe(false)
                .infected(true)
                .virusName(virusName)
                .message("发现病毒: " + virusName)
                .scannerName(scannerName)
                .build();
    }

    /**
     * 创建扫描失败的结果
     */
    public static VirusScanResult error(String scannerName, String errorMessage) {
        return VirusScanResult.builder()
                .safe(false)
                .infected(false)
                .message("扫描失败: " + errorMessage)
                .scannerName(scannerName)
                .build();
    }
}
