package com.clientservice.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 存储策略工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageStrategyFactory {

    private final LocalStorageStrategy localStorageStrategy;
    private final MinIOStorageStrategy minIOStorageStrategy;
    private final AliyunOSSStorageStrategy aliyunOSSStorageStrategy;

    @Value("${client-service.file.storage.type:local}")
    private String storageType;

    /**
     * 获取存储策略
     *
     * @return 存储策略实例
     */
    public StorageStrategy getStorageStrategy() {
        switch (storageType.toUpperCase()) {
            case "MINIO":
                log.info("使用MinIO对象存储");
                return minIOStorageStrategy;
            case "OSS":
                log.info("使用阿里云OSS对象存储");
                return aliyunOSSStorageStrategy;
            case "LOCAL":
            default:
                log.info("使用本地文件存储");
                return localStorageStrategy;
        }
    }
}
