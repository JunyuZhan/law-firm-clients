package com.clientservice.infrastructure.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 阿里云OSS对象存储策略
 */
@Slf4j
@Component
public class AliyunOSSStorageStrategy implements StorageStrategy {

    @Value("${client-service.file.storage.oss.endpoint:}")
    private String endpoint;

    @Value("${client-service.file.storage.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${client-service.file.storage.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${client-service.file.storage.oss.bucket-name:client-service}")
    private String bucketName;

    private volatile OSS ossClient;
    private final Object clientLock = new Object();

    /**
     * 获取OSS客户端（懒加载，线程安全）
     * 使用双重检查锁定确保多线程环境下只创建一个实例
     */
    private OSS getOssClient() {
        if (ossClient == null) {
            synchronized (clientLock) {
                if (ossClient == null) {
                    if (endpoint == null || endpoint.isEmpty()) {
                        throw new IllegalStateException("OSS endpoint未配置");
                    }
                    if (accessKeyId == null || accessKeyId.isEmpty()) {
                        throw new IllegalStateException("OSS access-key-id未配置");
                    }
                    if (accessKeySecret == null || accessKeySecret.isEmpty()) {
                        throw new IllegalStateException("OSS access-key-secret未配置");
                    }

                    ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                    log.info("OSS客户端初始化成功: endpoint={}, bucket={}", endpoint, bucketName);
                }
            }
        }
        return ossClient;
    }

    /**
     * 确保Bucket存在
     */
    private void ensureBucketExists() throws Exception {
        try {
            boolean exists = getOssClient().doesBucketExist(bucketName);
            if (!exists) {
                getOssClient().createBucket(bucketName);
                log.info("创建OSS Bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("OSS操作失败", e);
            throw new Exception("OSS操作失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String relativePath) throws Exception {
        ensureBucketExists();

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    relativePath,
                    inputStream
            );
            
            // 设置ContentType和ContentLength
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }
            metadata.setContentLength(file.getSize());
            putObjectRequest.setMetadata(metadata);

            getOssClient().putObject(putObjectRequest);

            log.debug("OSS文件上传成功: bucket={}, object={}", bucketName, relativePath);
            return relativePath;
        } catch (Exception e) {
            log.error("OSS文件上传失败: {}", relativePath, e);
            throw new Exception("OSS文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(String relativePath) throws Exception {
        // 参数校验
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        try {
            if (!exists(relativePath)) {
                throw new Exception("文件不存在: " + relativePath);
            }

            OSSObject ossObject = getOssClient().getObject(bucketName, relativePath);
            // 读取完整内容后立即关闭 OSSObject，避免资源泄漏
            byte[] content;
            try (InputStream inputStream = ossObject.getObjectContent()) {
                content = inputStream.readAllBytes();
            } finally {
                ossObject.close();
            }

            return new ByteArrayResource(content);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS文件下载失败: {}", relativePath, e);
            throw new Exception("OSS文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String relativePath) throws Exception {
        try {
            getOssClient().deleteObject(bucketName, relativePath);
            log.debug("OSS文件删除成功: bucket={}, object={}", bucketName, relativePath);
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", relativePath, e);
            throw new Exception("OSS文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String relativePath) throws Exception {
        try {
            return getOssClient().doesObjectExist(bucketName, relativePath);
        } catch (Exception e) {
            log.error("OSS检查文件存在性失败: {}", relativePath, e);
            throw new Exception("OSS检查文件存在性失败: " + e.getMessage());
        }
    }
}
