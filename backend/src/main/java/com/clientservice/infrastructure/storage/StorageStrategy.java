package com.clientservice.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略接口
 */
public interface StorageStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param relativePath 相对路径（如：matters/{matterId}/{fileId}.ext）
     * @return 存储路径
     */
    String uploadFile(MultipartFile file, String relativePath) throws Exception;

    /**
     * 下载文件
     *
     * @param relativePath 相对路径
     * @return 文件资源
     */
    Resource downloadFile(String relativePath) throws Exception;

    /**
     * 删除文件
     *
     * @param relativePath 相对路径
     */
    void deleteFile(String relativePath) throws Exception;

    /**
     * 检查文件是否存在
     *
     * @param relativePath 相对路径
     * @return 是否存在
     */
    boolean exists(String relativePath) throws Exception;
}
