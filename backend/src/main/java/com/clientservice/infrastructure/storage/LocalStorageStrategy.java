package com.clientservice.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储策略
 */
@Slf4j
@Component
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${client-service.file.storage.local.path:/data/client-service/files}")
    private String storageRootPath;

    // 最小可用空间阈值（100MB），低于此值拒绝上传
    private static final long MIN_USABLE_SPACE = 100 * 1024 * 1024L;

    @Override
    public String uploadFile(MultipartFile file, String relativePath) throws Exception {
        Path storagePath = Paths.get(storageRootPath, relativePath);

        try {
            // 确保目录存在
            Files.createDirectories(storagePath.getParent());
        } catch (AccessDeniedException e) {
            log.error("创建目录权限不足: {}", storagePath.getParent(), e);
            throw new IOException("无法创建存储目录，权限不足");
        }

        // 检查目录是否可写
        if (!Files.isWritable(storagePath.getParent())) {
            log.error("存储目录不可写: {}", storagePath.getParent());
            throw new IOException("存储目录不可写，请检查权限设置");
        }

        // 检查磁盘空间是否充足
        long fileSize = file.getSize();
        long usableSpace = storagePath.getParent().toFile().getUsableSpace();
        
        if (usableSpace < fileSize + MIN_USABLE_SPACE) {
            log.error("磁盘空间不足: 需要{}字节, 可用{}字节, 最小保留{}字节", 
                    fileSize, usableSpace, MIN_USABLE_SPACE);
            throw new IOException("磁盘空间不足，无法保存文件");
        }

        try {
            // 保存文件
            file.transferTo(storagePath.toFile());
        } catch (IOException e) {
            log.error("文件保存失败: path={}, error={}", storagePath, e.getMessage(), e);
            throw new IOException("文件保存失败: " + e.getMessage());
        }

        log.debug("本地文件上传成功: {}, 剩余空间: {}MB", storagePath, usableSpace / (1024 * 1024));
        return relativePath;
    }

    @Override
    public Resource downloadFile(String relativePath) throws Exception {
        Path filePath = Paths.get(storageRootPath, relativePath);
        
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("文件不存在: " + relativePath);
        }
        
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("路径不是文件: " + relativePath);
        }
        
        if (!Files.isReadable(filePath)) {
            log.error("文件不可读: {}", filePath);
            throw new AccessDeniedException("文件不可读，权限不足: " + relativePath);
        }
        
        return new UrlResource(filePath.toUri());
    }

    @Override
    public void deleteFile(String relativePath) throws Exception {
        Path filePath = Paths.get(storageRootPath, relativePath);
        
        if (!Files.exists(filePath)) {
            log.debug("文件不存在，跳过删除: {}", filePath);
            return;
        }
        
        try {
            Files.delete(filePath);
            log.debug("本地文件删除成功: {}", filePath);
        } catch (AccessDeniedException e) {
            log.error("文件删除权限不足: {}", filePath, e);
            throw new IOException("无法删除文件，权限不足");
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            throw new IOException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String relativePath) throws Exception {
        Path filePath = Paths.get(storageRootPath, relativePath);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
}
