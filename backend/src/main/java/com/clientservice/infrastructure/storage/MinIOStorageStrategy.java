package com.clientservice.infrastructure.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO对象存储策略
 */
@Slf4j
@Component
public class MinIOStorageStrategy implements StorageStrategy {

    @Value("${client-service.file.storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${client-service.file.storage.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${client-service.file.storage.minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${client-service.file.storage.minio.bucket-name:client-service}")
    private String bucketName;

    private volatile MinioClient minioClient;
    private final Object clientLock = new Object();

    /**
     * 获取MinIO客户端（懒加载，线程安全）
     * 使用双重检查锁定确保多线程环境下只创建一个实例
     */
    private MinioClient getMinioClient() {
        if (minioClient == null) {
            synchronized (clientLock) {
                if (minioClient == null) {
                    minioClient = MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();
                }
            }
        }
        return minioClient;
    }

    /**
     * 确保Bucket存在
     */
    private void ensureBucketExists() throws Exception {
        try {
            boolean found = getMinioClient().bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!found) {
                getMinioClient().makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建MinIO Bucket: {}", bucketName);
            }
        } catch (MinioException e) {
            log.error("MinIO操作失败", e);
            throw new Exception("MinIO操作失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String relativePath) throws Exception {
        ensureBucketExists();

        try (InputStream inputStream = file.getInputStream()) {
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(relativePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.debug("MinIO文件上传成功: bucket={}, object={}", bucketName, relativePath);
            return relativePath;
        } catch (MinioException e) {
            log.error("MinIO文件上传失败: {}", relativePath, e);
            throw new Exception("MinIO文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(String relativePath) throws Exception {
        try (InputStream inputStream = getMinioClient().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(relativePath)
                        .build()
        )) {
            // 读取为 byte[] 后立即关闭 MinIO 连接，避免资源泄漏
            byte[] content = inputStream.readAllBytes();
            return new ByteArrayResource(content);
        } catch (MinioException e) {
            log.error("MinIO文件下载失败: {}", relativePath, e);
            throw new Exception("MinIO文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String relativePath) throws Exception {
        try {
            getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(relativePath)
                            .build()
            );

            log.debug("MinIO文件删除成功: bucket={}, object={}", bucketName, relativePath);
        } catch (MinioException e) {
            log.error("MinIO文件删除失败: {}", relativePath, e);
            throw new Exception("MinIO文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String relativePath) throws Exception {
        try {
            getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(relativePath)
                            .build()
            );
            return true;
        } catch (MinioException e) {
            // MinIO SDK中，文件不存在时会抛出MinioException
            // 检查错误消息或错误代码来判断文件是否存在
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("NoSuchKey") || errorMessage.contains("does not exist"))) {
                return false;
            }
            log.error("MinIO检查文件存在性失败: {}", relativePath, e);
            throw new Exception("MinIO检查文件存在性失败: " + e.getMessage());
        }
    }
}
