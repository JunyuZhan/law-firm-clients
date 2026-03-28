package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.dto.ClientFileUploadRequest;
import com.clientservice.application.service.CallbackService;
import com.clientservice.application.service.DownloadLogService;
import com.clientservice.application.service.FileService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ClientMatter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件控制器
 */
@Slf4j
@Tag(name = "文件管理", description = "客户文件上传和管理")
@RestController
@RequestMapping("/api/client/files")
@RequiredArgsConstructor
public class FileController {

    /** 允许内联预览的 MIME 类型白名单 */
    private static final Set<String> PREVIEWABLE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml", "image/bmp",
            "application/pdf",
            "text/plain", "text/csv", "text/html",
            "audio/mpeg", "audio/wav", "audio/ogg",
            "video/mp4", "video/webm"
    );

    /** 获取安全的预览 Content-Type，非白名单类型强制为 application/octet-stream */
    private String getSafePreviewContentType(String fileType) {
        if (fileType != null && PREVIEWABLE_MIME_TYPES.contains(fileType.toLowerCase())) {
            return fileType;
        }
        return "application/octet-stream";
    }

    private final FileService fileService;
    private final MatterService matterService;
    private final DownloadLogService downloadLogService;
    private final CallbackService callbackService;

    /**
     * 上传文件
     *
     * @param matterId 项目ID
     * @param clientId 客户ID
     * @param token 访问令牌
     * @param file 文件
     * @param fileCategory 文件类别
     * @param description 文件描述
     * @return 文件DTO
     */
    @Operation(summary = "上传文件", description = "客户上传文件到项目")
    @Timed(value = "api.file.upload", description = "文件上传接口耗时")
    @PostMapping("/upload")
    public Result<ClientFileDTO> uploadFile(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "客户ID", required = true) @RequestParam final Long clientId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "文件", required = true) @RequestParam final MultipartFile file,
            @Parameter(description = "文件类别") @RequestParam(required = false) final String fileCategory,
            @Parameter(description = "文件描述") @RequestParam(required = false) final String description) {

        // 参数长度校验
        if (matterId != null && matterId.length() > 100) {
            return Result.badRequest("项目ID长度不能超过100字符");
        }
        if (token != null && token.length() > 500) {
            return Result.badRequest("访问令牌长度异常");
        }
        if (fileCategory != null && fileCategory.length() > 50) {
            return Result.badRequest("文件类别长度不能超过50字符");
        }
        if (description != null && description.length() > 1000) {
            return Result.badRequest("文件描述长度不能超过1000字符");
        }

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return Result.forbidden("项目ID不匹配");
        }

        // 构建上传请求
        ClientFileUploadRequest request = new ClientFileUploadRequest();
        request.setMatterId(matterId);
        request.setClientId(clientId);
        request.setFile(file);
        request.setFileCategory(fileCategory);
        request.setDescription(description);

        // 上传文件
        ClientFileDTO fileDTO = fileService.uploadFile(request);

        // 异步回调通知管理系统（失败不影响文件上传）
        try {
            // 获取文件实体用于回调
            com.clientservice.domain.entity.ClientFile clientFile = fileService.getFileEntity(fileDTO.getId());
            callbackService.callbackUploadFile(clientFile, matter, token);
        } catch (Exception e) {
            // 回调失败不影响文件上传
            log.warn("文件上传回调失败: fileId={}, matterId={}", fileDTO.getId(), matterId, e);
        }

        return Result.success(fileDTO);
    }

    /**
     * 获取文件列表
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param status 状态（可选）
     * @return 文件列表
     */
    @Operation(summary = "获取文件列表", description = "获取项目的文件列表")
    @Timed(value = "api.file.list", description = "获取文件列表接口耗时")
    @GetMapping
    public Result<List<ClientFileDTO>> getFiles(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "状态") @RequestParam(required = false) final String status) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return Result.forbidden("项目ID不匹配");
        }

        // 获取文件列表
        List<ClientFileDTO> files = fileService.getFilesByMatterId(matterId, status);

        return Result.success(files);
    }

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件DTO
     */
    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    public Result<ClientFileDTO> getFile(
            @Parameter(description = "文件ID", required = true) @PathVariable final String fileId) {
        ClientFileDTO file = fileService.getFileById(fileId);
        return Result.success(file);
    }

    /**
     * 下载文件
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param fileId 文件ID
     * @return 文件资源
     */
    @Operation(summary = "下载文件", description = "下载项目中的文件")
    @Timed(value = "api.file.download", description = "文件下载接口耗时")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "文件ID", required = true) @PathVariable final String fileId,
            @Parameter(description = "是否系统同步（系统同步不记录下载日志）") @RequestParam(required = false, defaultValue = "false") final boolean systemSync,
            final HttpServletRequest request) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 获取文件实体
        com.clientservice.domain.entity.ClientFile file = fileService.getFileEntity(fileId);
        if (!file.getMatterId().equals(matterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 记录下载日志（系统同步时跳过，异步，失败不影响下载）
        if (!systemSync) {
            try {
                // 从项目信息获取客户ID（如果文件中的clientId为null）
                Long clientId = file.getClientId() != null ? file.getClientId() : matter.getClientId();
                downloadLogService.recordDownload(
                        matterId,
                        clientId,
                        fileId,
                        file.getFileName(),
                        token,
                        request);
            } catch (Exception e) {
                // 下载日志记录失败不影响文件下载
                log.warn("记录文件下载日志失败: fileId={}, matterId={}", fileId, matterId, e);
            }
        } else {
            log.debug("系统同步下载，跳过下载日志记录: fileId={}", fileId);
        }

        // 记录推送文件首次下载时间（如果是推送文件且首次下载）
        try {
            fileService.recordFileDownload(fileId);
        } catch (Exception e) {
            // 首次下载时间记录失败不影响文件下载
            log.warn("记录推送文件首次下载时间失败: fileId={}", fileId, e);
        }

        // 获取文件资源
        Resource resource = fileService.getFileResource(fileId);

        // 设置响应头（清理文件名中的特殊字符，防止安全问题）
        String safeFileName = file.getFileName() != null ? file.getFileName() : "file";
        safeFileName = safeFileName.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
        String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        file.getFileType() != null ? file.getFileType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    /**
     * 预览文件（内联显示，不下载）
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param fileId 文件ID
     * @return 文件资源
     */
    @Operation(summary = "预览文件", description = "预览项目中的文件（内联显示）")
    @Timed(value = "api.file.preview", description = "文件预览接口耗时")
    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "文件ID", required = true) @PathVariable final String fileId) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 获取文件实体
        com.clientservice.domain.entity.ClientFile file = fileService.getFileEntity(fileId);
        if (!file.getMatterId().equals(matterId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 获取文件资源
        Resource resource = fileService.getFileResource(fileId);

        // 设置响应头（内联显示，MIME 类型经白名单校验防止伪造）
        String safeContentType = getSafePreviewContentType(file.getFileType());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(safeContentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    /**
     * 删除文件
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param fileId 文件ID
     * @return 空结果
     */
    @Operation(summary = "删除文件", description = "删除项目中的文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "项目ID", required = true) @RequestParam final String matterId,
            @Parameter(description = "访问令牌", required = true) @RequestParam final String token,
            @Parameter(description = "文件ID", required = true) @PathVariable final String fileId) {

        // 验证令牌和项目
        ClientMatter matter = matterService.getMatterByToken(token);
        if (!matter.getId().equals(matterId)) {
            return Result.forbidden("项目ID不匹配");
        }

        // 验证文件是否属于该项目
        ClientFileDTO file = fileService.getFileById(fileId);
        if (!file.getMatterId().equals(matterId)) {
            return Result.forbidden("文件不属于该项目");
        }

        // 删除文件
        fileService.deleteFile(fileId);

        return Result.success();
    }
}
