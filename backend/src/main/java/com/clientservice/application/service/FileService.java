package com.clientservice.application.service;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.dto.ClientFileUploadRequest;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.TokenGenerator;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientFileMapper;
import com.clientservice.infrastructure.scanner.VirusScanResult;
import com.clientservice.infrastructure.scanner.VirusScannerFactory;
import com.clientservice.infrastructure.storage.StorageStrategy;
import com.clientservice.infrastructure.storage.StorageStrategyFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final String SOURCE_API_KEY_ID_KEY = "_sourceApiKeyId";

    private final ClientFileMapper fileMapper;
    private final ClientMatterMapper matterMapper;
    private final StorageStrategyFactory storageStrategyFactory;
    private final VirusScannerFactory virusScannerFactory;
    private final ObjectMapper objectMapper;

    /** 最大文件大小（字节），默认10MB */
    @Value("${client-service.file.max-size:10485760}")
    private long maxFileSize;

    /** 允许的文件扩展名（逗号分隔），默认允许常见文档和图片格式 */
    @Value("${client-service.file.allowed-extensions:.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.bmp,.txt,.zip,.rar}")
    private String allowedExtensions;

    /** 是否启用文件扩展名验证 */
    @Value("${client-service.file.validate-extension:true}")
    private boolean validateExtension;

    /** 是否启用文件内容验证（文件头魔数） */
    @Value("${client-service.file.validate-content:true}")
    private boolean validateContent;

    /**
     * 计算文件SHA-256哈希值
     */
    private String calculateFileHash(final MultipartFile file) {
        try (java.io.InputStream inputStream = file.getInputStream()) {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("计算文件哈希失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "计算文件哈希失败");
        }
    }

    /**
     * 上传文件
     *
     * @param request 上传请求
     * @return 文件DTO
     */
    @Transactional
    public ClientFileDTO uploadFile(final ClientFileUploadRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传请求不能为空");
        }
        
        MultipartFile multipartFile = request.getFile();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }

        // 1. 验证文件
        validateFile(multipartFile);

        // 2. 病毒扫描
        scanForVirus(multipartFile);

        // 3. 计算文件哈希并检查幂等性
        String fileHash = calculateFileHash(multipartFile);
        ClientFile existingFile = fileMapper.selectByMatterIdAndFileHash(request.getMatterId(), fileHash);
        if (existingFile != null) {
            log.info("检测到重复文件上传，返回已有记录: fileId={}, matterId={}, fileName={}", 
                    existingFile.getId(), request.getMatterId(), existingFile.getFileName());
            return convertToDTO(existingFile);
        }

        // 4. 生成文件ID
        String fileId = TokenGenerator.generateId();

        // 4. 获取文件扩展名
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 5. 构建存储路径（包含文件ID和扩展名）
        // 防止路径遍历：验证 matterId 格式
        String matterId = request.getMatterId();
        if (matterId == null || !matterId.matches("^[a-zA-Z0-9\\-_]+$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的项目ID格式");
        }
        String relativePath = String.format("matters/%s/%s%s", 
                matterId, fileId, extension);

        // 6. 使用存储策略上传文件
        StorageStrategy storageStrategy = storageStrategyFactory.getStorageStrategy();
        try {
            storageStrategy.uploadFile(multipartFile, relativePath);
        } catch (Exception e) {
            log.error("文件上传失败: {}", relativePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "文件上传失败: " + e.getMessage());
        }

        // 7. 创建文件记录
        ClientFile clientFile = ClientFile.builder()
                .id(fileId)
                .matterId(request.getMatterId())
                .clientId(request.getClientId())
                .fileName(multipartFile.getOriginalFilename())
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .fileCategory(request.getFileCategory() != null 
                    ? request.getFileCategory() 
                    : ClientFile.CATEGORY_OTHER)
                .description(request.getDescription())
                .storagePath(relativePath)
                .fileSource(ClientFile.SOURCE_CLIENT_UPLOAD) // 客户上传的文件
                .fileHash(fileHash) // 保存文件哈希
                .uploadedAt(LocalDateTime.now())
                .status(ClientFile.STATUS_ACTIVE)
                .build();

        fileMapper.insert(clientFile);

        log.info("文件上传成功: fileId={}, matterId={}, fileName={}", 
                fileId, request.getMatterId(), multipartFile.getOriginalFilename());

        return convertToDTO(clientFile);
    }

    /**
     * 验证文件
     *
     * @param file 文件
     */
    private void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, 
                String.format("文件大小超过限制（最大%dMB）", maxFileSize / 1024 / 1024));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件名不能为空");
        }

        // 1. 验证文件扩展名（白名单）
        if (validateExtension) {
            validateFileExtension(originalFilename);
        }

        // 2. 验证文件内容（文件头魔数）
        if (validateContent) {
            validateFileContent(file);
        }
    }

    /**
     * 验证文件扩展名（白名单）
     *
     * @param filename 文件名
     */
    private void validateFileExtension(final String filename) {
        // 获取文件扩展名（转小写）
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            extension = filename.substring(lastDotIndex).toLowerCase();
        }

        if (extension.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件必须包含扩展名");
        }

        // 检查扩展名是否在白名单中
        String[] allowedExts = allowedExtensions.split(",");
        boolean isAllowed = false;
        for (String allowedExt : allowedExts) {
            if (extension.equals(allowedExt.trim().toLowerCase())) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, 
                String.format("不允许的文件类型：%s。允许的类型：%s", extension, allowedExtensions));
        }
    }

    /**
     * 验证文件内容（文件头魔数）
     * 通过检查文件头部的字节序列来验证文件类型，防止通过修改扩展名绕过验证
     *
     * @param file 文件
     */
    private void validateFileContent(final MultipartFile file) {
        // 使用 try-with-resources 确保流正确关闭，避免资源泄漏
        try (java.io.InputStream rawStream = file.getInputStream();
             java.io.BufferedInputStream inputStream = new java.io.BufferedInputStream(rawStream)) {
            
            // 标记当前位置，以便后续重置
            inputStream.mark(16);
            
            byte[] fileHeader = new byte[8];
            int bytesRead = inputStream.read(fileHeader);
            
            // 重置流位置（BufferedInputStream 支持 mark/reset）
            // 注意：reset操作是可选的，因为流在try-with-resources中管理，
            // 验证完成后会自动关闭，不影响后续文件上传操作
            try {
                inputStream.reset();
            } catch (java.io.IOException e) {
                // reset失败不影响文件验证，因为文件头已读取完成
                log.debug("输入流reset失败（不影响验证）: {}", e.getMessage());
            }
            
            if (bytesRead < 4) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "文件内容无效或已损坏");
            }

            String filename = file.getOriginalFilename();
            if (filename == null) {
                return;
            }

            String extension = "";
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
                extension = filename.substring(lastDotIndex).toLowerCase();
            }

            // 验证文件头魔数
            boolean isValid = validateFileHeader(extension, fileHeader);

            if (!isValid) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, 
                    String.format("文件内容与扩展名不匹配：%s。文件可能已损坏或被篡改。", extension));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (java.io.IOException e) {
            log.error("文件内容验证IO异常: fileName={}, error={}", 
                    file.getOriginalFilename(), e.getMessage());
            // IO异常时拒绝上传，防止攻击者利用异常绕过验证
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件读取失败，无法验证文件内容");
        } catch (Exception e) {
            log.error("文件内容验证异常: fileName={}, error={}", 
                    file.getOriginalFilename(), e.getMessage());
            // 未知异常时拒绝上传，确保安全性
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件验证失败，请确保文件未损坏");
        }
    }

    /**
     * 验证文件头魔数
     *
     * @param extension 文件扩展名
     * @param fileHeader 文件头字节
     * @return 是否有效
     */
    private boolean validateFileHeader(final String extension, final byte[] fileHeader) {
        // PDF: %PDF
        if (".pdf".equals(extension)) {
            return fileHeader[0] == 0x25 && fileHeader[1] == 0x50 && 
                   fileHeader[2] == 0x44 && fileHeader[3] == 0x46;
        }
        // ZIP/Office新格式: PK (ZIP格式)
        if (".zip".equals(extension) || ".docx".equals(extension) || 
            ".xlsx".equals(extension) || ".pptx".equals(extension)) {
            return fileHeader[0] == 0x50 && fileHeader[1] == 0x4B; // PK
        }
        // RAR: Rar!
        if (".rar".equals(extension)) {
            return fileHeader[0] == 0x52 && fileHeader[1] == 0x61 && 
                   fileHeader[2] == 0x72 && fileHeader[3] == 0x21;
        }
        // JPEG: FF D8 FF
        if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
            return fileHeader[0] == (byte)0xFF && fileHeader[1] == (byte)0xD8 && 
                   fileHeader[2] == (byte)0xFF;
        }
        // PNG: 89 50 4E 47
        if (".png".equals(extension)) {
            return fileHeader[0] == (byte)0x89 && fileHeader[1] == 0x50 && 
                   fileHeader[2] == 0x4E && fileHeader[3] == 0x47;
        }
        // GIF: GIF87a 或 GIF89a
        if (".gif".equals(extension)) {
            return (fileHeader[0] == 0x47 && fileHeader[1] == 0x49 && 
                   fileHeader[2] == 0x46 && fileHeader[3] == 0x38 && 
                   (fileHeader[4] == 0x37 || fileHeader[4] == 0x39) && 
                   fileHeader[5] == 0x61);
        }
        // BMP: BM
        if (".bmp".equals(extension)) {
            return fileHeader[0] == 0x42 && fileHeader[1] == 0x4D;
        }
        // Office旧格式 (DOC, XLS, PPT): D0 CF 11 E0 (OLE2格式)
        if (".doc".equals(extension) || ".xls".equals(extension) || ".ppt".equals(extension)) {
            return fileHeader[0] == (byte)0xD0 && fileHeader[1] == (byte)0xCF && 
                   fileHeader[2] == 0x11 && fileHeader[3] == (byte)0xE0;
        }
        // TXT: 纯文本文件，不进行严格验证（允许）
        if (".txt".equals(extension)) {
            return true;
        }
        // 其他类型：如果不在验证列表中，允许通过（向后兼容）
        return true;
    }

    /**
     * 病毒扫描
     *
     * @param file 文件
     */
    private void scanForVirus(final MultipartFile file) {
        try {
            VirusScanResult result = virusScannerFactory.getVirusScanner().scanFile(file);
            
            if (!result.isSafe()) {
                if (result.isInfected()) {
                    log.error("文件包含病毒: fileName={}, virus={}", 
                            file.getOriginalFilename(), result.getVirusName());
                    throw new BusinessException(ErrorCode.BAD_REQUEST, 
                            String.format("文件包含病毒，拒绝上传: %s", result.getVirusName()));
                } else {
                    // 扫描失败，根据配置决定是否允许上传
                    // 这里选择拒绝上传（更安全）
                    log.error("病毒扫描失败: fileName={}, error={}", 
                            file.getOriginalFilename(), result.getMessage());
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                            "病毒扫描失败，无法验证文件安全性: " + result.getMessage());
                }
            }
            
            log.debug("文件病毒扫描通过: fileName={}", file.getOriginalFilename());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("病毒扫描异常: fileName={}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "病毒扫描异常: " + e.getMessage());
        }
    }

    /**
     * 根据项目ID获取文件列表
     *
     * @param matterId 项目ID
     * @param status 状态（可选）
     * @return 文件列表
     */
    public List<ClientFileDTO> getFilesByMatterId(final String matterId, final String status) {
        List<ClientFile> files = fileMapper.selectByMatterId(matterId, status);
        return files.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取文件
     *
     * @param fileId 文件ID
     * @return 文件DTO
     */
    public ClientFileDTO getFileById(final String fileId) {
        ClientFile file = fileMapper.selectById(fileId);
        if (file == null || file.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return convertToDTO(file);
    }

    /**
     * 根据ID获取可供客户访问的有效文件。
     *
     * @param fileId 文件ID
     * @return 文件DTO
     */
    public ClientFileDTO getActiveFileById(final String fileId) {
        ClientFile file = loadRequiredActiveFile(fileId);
        return convertToDTO(file);
    }

    /**
     * 获取文件资源（用于下载）
     *
     * @param fileId 文件ID
     * @return 文件资源
     */
    public org.springframework.core.io.Resource getFileResource(final String fileId) {
        ClientFile file = fileMapper.selectById(fileId);
        if (file == null || file.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }

        if (!ClientFile.STATUS_ACTIVE.equals(file.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不可用");
        }

        StorageStrategy storageStrategy = storageStrategyFactory.getStorageStrategy();
        try {
            return storageStrategy.downloadFile(file.getStoragePath());
        } catch (Exception e) {
            log.error("获取文件资源失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "获取文件资源失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件实体（用于下载）
     *
     * @param fileId 文件ID
     * @return 文件实体
     */
    public ClientFile getFileEntity(final String fileId) {
        ClientFile file = fileMapper.selectById(fileId);
        if (file == null || file.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    /**
     * 获取可供客户访问的有效文件实体。
     *
     * @param fileId 文件ID
     * @return 文件实体
     */
    public ClientFile getActiveFileEntity(final String fileId) {
        return loadRequiredActiveFile(fileId);
    }

    /**
     * 记录文件下载（用于推送文件首次下载时间更新）
     *
     * @param fileId 文件ID
     */
    @Transactional
    public void recordFileDownload(final String fileId) {
        ClientFile file = fileMapper.selectById(fileId);
        if (file == null || file.getDeleted()) {
            return;
        }

        // 检查是否为推送文件（通过检查是否有外部文件URL或其他标识）
        // 注意：如果 ClientFile 实体中没有 fileSource 字段，此功能暂时不实现
        // 如果后续需要实现推送文件首次下载时间更新，需要：
        // 1. 在 ClientFile 实体中添加 fileSource 字段（PUSHED/CLIENT_UPLOAD）
        // 2. 在 ClientFile 实体中添加 firstDownloadedAt 字段
        // 3. 更新此方法以检查 fileSource == PUSHED 且 firstDownloadedAt == null 时更新首次下载时间
        
        log.debug("记录文件下载: fileId={}", fileId);
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    @Transactional
    public void deleteFile(final String fileId) {
        performDelete(loadRequiredFile(fileId));
    }

    /**
     * 删除律所系统来源的文件回调，并校验来源 API Key 归属。
     *
     * @param fileId 文件ID
     * @param sourceApiKeyId 回调来源 API Key ID
     */
    @Transactional
    public void deleteFileForSource(final String fileId, final Long sourceApiKeyId) {
        ClientFile file = loadRequiredFile(fileId);
        ClientMatter matter = matterMapper.selectById(file.getMatterId());
        if (matter == null || matter.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }

        Long boundApiKeyId = extractSourceApiKeyId(matter);
        if (boundApiKeyId == null || !Objects.equals(boundApiKeyId, sourceApiKeyId)) {
            log.warn(
                    "拒绝删除不属于当前来源的文件: fileId={}, matterId={}, sourceApiKeyId={}, boundApiKeyId={}",
                    fileId,
                    file.getMatterId(),
                    sourceApiKeyId,
                    boundApiKeyId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除该文件");
        }

        performDelete(file);
    }

    private ClientFile loadRequiredFile(final String fileId) {
        ClientFile file = fileMapper.selectById(fileId);
        if (file == null || file.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    private ClientFile loadRequiredActiveFile(final String fileId) {
        ClientFile file = loadRequiredFile(fileId);
        if (!ClientFile.STATUS_ACTIVE.equals(file.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    private Long extractSourceApiKeyId(final ClientMatter matter) {
        if (matter == null) {
            return null;
        }
        if (matter.getSourceApiKeyId() != null) {
            return matter.getSourceApiKeyId();
        }
        String matterData = matter.getMatterData();
        if (matterData == null || matterData.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(matterData);
            JsonNode apiKeyNode = root.get(SOURCE_API_KEY_ID_KEY);
            if (apiKeyNode == null || apiKeyNode.isNull()) {
                return null;
            }
            return apiKeyNode.isNumber() ? apiKeyNode.longValue() : Long.parseLong(apiKeyNode.asText());
        } catch (Exception e) {
            log.warn("解析项目来源 API Key 失败: matterId={}", matter.getId(), e);
            return null;
        }
    }

    private void performDelete(final ClientFile file) {
        // 删除物理文件
        StorageStrategy storageStrategy = storageStrategyFactory.getStorageStrategy();
        try {
            storageStrategy.deleteFile(file.getStoragePath());
        } catch (Exception e) {
            log.warn("删除物理文件失败: {}", file.getStoragePath(), e);
        }

        // 更新文件状态
        file.setStatus(ClientFile.STATUS_DELETED);
        fileMapper.updateById(file);

        log.info("文件删除成功: fileId={}", file.getId());
    }

    /**
     * 转换为DTO
     *
     * @param file 文件实体
     * @return 文件DTO
     */
    private ClientFileDTO convertToDTO(final ClientFile file) {
        return ClientFileDTO.builder()
                .id(file.getId())
                .matterId(file.getMatterId())
                .clientId(file.getClientId())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .fileCategory(file.getFileCategory())
                .description(file.getDescription())
                .storagePath(file.getStoragePath())
                .fileSource(file.getFileSource() != null 
                    ? file.getFileSource() 
                    : ClientFile.SOURCE_CLIENT_UPLOAD) // 默认值：客户上传
                .fileHash(file.getFileHash())
                .uploadedAt(file.getUploadedAt())
                .status(file.getStatus())
                .build();
    }

    /**
     * 分页获取文件列表（管理员）
     *
     * @param matterId 项目ID（可选）
     * @param status 状态（可选）
     * @param fileCategory 文件类别（可选）
     * @param keyword 关键字（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    public Map<String, Object> getFilesWithPagination(
            final String matterId, 
            final String status,
            final String fileCategory,
            final String keyword,
            final int page, 
            final int pageSize) {
        
        int offset = (page - 1) * pageSize;
        
        List<ClientFile> files = fileMapper.selectWithPagination(
            matterId, status, fileCategory, keyword, offset, pageSize);
        Long total = fileMapper.countWithFilter(matterId, status, fileCategory, keyword);
        
        List<ClientFileDTO> fileDTOs = files.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", fileDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        
        return result;
    }

    /**
     * 获取文件统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总文件数
        Long totalCount = fileMapper.countWithFilter(null, null, null, null);
        stats.put("totalCount", totalCount);
        
        // 活跃文件数
        Long activeCount = fileMapper.countWithFilter(null, ClientFile.STATUS_ACTIVE, null, null);
        stats.put("activeCount", activeCount);
        
        // 已删除文件数
        Long deletedCount = fileMapper.countWithFilter(null, ClientFile.STATUS_DELETED, null, null);
        stats.put("deletedCount", deletedCount);
        
        // 总存储大小
        Long totalSize = fileMapper.sumFileSize(null);
        stats.put("totalSize", totalSize != null ? totalSize : 0L);
        stats.put("totalSizeFormatted", formatFileSize(totalSize != null ? totalSize : 0L));
        
        // 活跃文件存储大小
        Long activeSize = fileMapper.sumFileSize(ClientFile.STATUS_ACTIVE);
        stats.put("activeSize", activeSize != null ? activeSize : 0L);
        stats.put("activeSizeFormatted", formatFileSize(activeSize != null ? activeSize : 0L));
        
        // 按类别统计
        Map<String, Long> categoryStats = new HashMap<>();
        categoryStats.put("EVIDENCE", fileMapper.countByCategory("EVIDENCE"));
        categoryStats.put("CONTRACT", fileMapper.countByCategory("CONTRACT"));
        categoryStats.put("ID_CARD", fileMapper.countByCategory("ID_CARD"));
        categoryStats.put("OTHER", fileMapper.countByCategory("OTHER"));
        stats.put("categoryStats", categoryStats);
        
        // 项目数量（有文件的项目）
        Long matterCount = fileMapper.countDistinctMatters();
        stats.put("matterCount", matterCount);
        
        return stats;
    }

    /**
     * 清理已删除的文件
     *
     * @param days 删除N天前标记为删除的文件
     * @return 清理数量
     */
    @Transactional
    public int cleanupDeletedFiles(final int days) {
        LocalDateTime before = LocalDateTime.now().minusDays(days);
        
        int cleanedCount = 0;
        StorageStrategy storageStrategy = storageStrategyFactory.getStorageStrategy();
        
        // 分批查询与清理，每批最多 500 条（由 SQL LIMIT 控制），防止内存溢出
        List<ClientFile> filesToClean;
        while (!(filesToClean = fileMapper.selectDeletedBefore(before)).isEmpty()) {
            for (ClientFile file : filesToClean) {
                try {
                    String storagePath = file.getStoragePath();
                    // 检查存储路径是否有效
                    if (storagePath == null || storagePath.isBlank()) {
                        log.warn("文件存储路径为空，仅删除数据库记录: fileId={}", file.getId());
                    } else {
                        // 删除物理文件
                        storageStrategy.deleteFile(storagePath);
                    }
                    // 物理删除数据库记录
                    fileMapper.deleteById(file.getId());
                    cleanedCount++;
                    log.debug("清理文件成功: fileId={}, storagePath={}", file.getId(), storagePath);
                } catch (Exception e) {
                    log.warn("清理文件失败: fileId={}, error={}", file.getId(), e.getMessage());
                }
            }
        }
        
        return cleanedCount;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
