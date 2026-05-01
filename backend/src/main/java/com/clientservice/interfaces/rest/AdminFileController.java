package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.FileService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员文件管理控制器
 */
@Slf4j
@Tag(name = "管理员文件管理", description = "文件查询、统计和管理")
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileService fileService;
    private final AdminAuthorizationService adminAuthorizationService;

    /**
     * 获取文件列表（支持分页和筛选）
     */
    @Operation(summary = "获取文件列表", description = "获取所有文件，支持按项目、状态筛选")
    @GetMapping
    public Result<Map<String, Object>> getFiles(
            @Parameter(description = "项目ID") @RequestParam(required = false) String matterId,
            @Parameter(description = "文件状态") @RequestParam(required = false) String status,
            @Parameter(description = "文件类别") @RequestParam(required = false) String fileCategory,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        adminAuthorizationService.requireSuperAdmin();
        // 分页参数安全校验
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(100, pageSize));
        
        Map<String, Object> result = fileService.getFilesWithPagination(
            matterId, status, fileCategory, keyword, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取文件统计信息
     */
    @Operation(summary = "获取文件统计", description = "获取文件存储统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        adminAuthorizationService.requireSuperAdmin();
        Map<String, Object> stats = fileService.getFileStatistics();
        return Result.success(stats);
    }

    /**
     * 获取文件详情
     */
    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    public Result<ClientFileDTO> getFile(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId) {
        adminAuthorizationService.requireSuperAdmin();
        ClientFileDTO file = fileService.getFileById(fileId);
        return Result.success(file);
    }

    /**
     * 删除文件（管理员）
     */
    @Operation(summary = "删除文件", description = "管理员删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable String fileId) {
        adminAuthorizationService.requireSuperAdmin();
        fileService.deleteFile(fileId);
        log.info("管理员删除文件: fileId={}", fileId);
        return Result.success();
    }

    /**
     * 批量删除文件
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个文件（单次最多100个）")
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDeleteFiles(
            @Parameter(description = "文件ID列表", required = true) @RequestBody List<String> fileIds) {
        adminAuthorizationService.requireSuperAdmin();
        if (fileIds == null || fileIds.isEmpty()) {
            return Result.badRequest("文件ID列表不能为空");
        }
        if (fileIds.size() > 100) {
            return Result.badRequest("单次批量删除不能超过100个文件");
        }
        int successCount = 0;
        int failCount = 0;
        
        for (String fileId : fileIds) {
            try {
                fileService.deleteFile(fileId);
                successCount++;
            } catch (Exception e) {
                log.warn("删除文件失败: fileId={}, error={}", fileId, e.getMessage());
                failCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        
        log.info("批量删除文件完成: successCount={}, failCount={}", successCount, failCount);
        return Result.success(result);
    }

    /**
     * 清理过期文件
     */
    @Operation(summary = "清理过期文件", description = "清理已删除状态的文件")
    @PostMapping("/cleanup")
    public Result<Map<String, Object>> cleanupFiles(
            @Parameter(description = "清理天数（删除N天前标记为删除的文件）") 
            @RequestParam(defaultValue = "30") int days) {
        adminAuthorizationService.requireSuperAdmin();
        int cleanedCount = fileService.cleanupDeletedFiles(days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("cleanedCount", cleanedCount);
        
        log.info("清理过期文件完成: cleanedCount={}, days={}", cleanedCount, days);
        return Result.success(result);
    }

    /**
     * 上传系统公共文件（如Logo）
     */
    @Operation(summary = "上传公共文件", description = "管理员上传系统公共文件，如Logo图片")
    @PostMapping("/public/upload")
    public Result<Map<String, String>> uploadPublicFile(
            @Parameter(description = "文件", required = true) @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        adminAuthorizationService.requireSuperAdmin();
        
        if (file == null || file.isEmpty()) {
            return Result.badRequest("上传文件不能为空");
        }
        
        try {
            // 生成唯一文件名，防止覆盖
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 简单的类型校验，主要允许图片
            if (!extension.matches("(?i)\\.(jpg|jpeg|png|gif|webp|svg)")) {
                return Result.badRequest("仅支持上传图片文件(jpg, png, gif, webp, svg)");
            }
            
            String fileId = com.clientservice.common.util.TokenGenerator.generateId();
            String relativePath = "public/images/" + fileId + extension;
            
            com.clientservice.infrastructure.storage.StorageStrategy storageStrategy = 
                org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext())
                .getBean(com.clientservice.infrastructure.storage.StorageStrategyFactory.class)
                .getStorageStrategy();
                
            String fileUrl = storageStrategy.uploadFile(file, relativePath);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("path", relativePath);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("公共文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
