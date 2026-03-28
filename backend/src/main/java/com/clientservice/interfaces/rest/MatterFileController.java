package com.clientservice.interfaces.rest;

import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.FileService;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ApiKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 项目文件控制器（供律所系统调用）
 */
@Slf4j
@Tag(name = "项目文件回调", description = "律所系统同步文件后的删除回调接口")
@RestController
@RequestMapping({"/api/files", "/api/matter/files"})
@RequiredArgsConstructor
public class MatterFileController {

    private final ApiKeyService apiKeyService;
    private final FileService fileService;

    /**
     * 文件删除回调
     * 
     * 支持两种参数传递方式：
     * 1. 查询参数：POST /api/files/delete?fileId=xxx（推荐）
     * 2. 请求体：POST /api/files/delete，Body: {"fileId": "xxx", "action": "DELETE"}
     *
     * @param authorization 认证头（Bearer Token）
     * @param fileId 外部文件ID（查询参数，优先使用）
     * @param requestBody 请求体（兼容旧版本，可选）
     * @return 空结果
     */
    @Operation(summary = "文件删除回调", description = "律所系统同步文件后，通知客户服务系统删除文件")
    @PostMapping("/delete")
    public Result<Void> deleteFile(
            @RequestHeader(value = "Authorization", required = false) final String authorization,
            @Parameter(description = "外部文件ID（查询参数）") @RequestParam(required = false) final String fileId,
            @RequestBody(required = false) final Map<String, Object> requestBody) {

        // 验证API密钥
        ApiKey apiKey = apiKeyService.validateApiKey(authorization);
        log.info("API密钥验证成功: keyName={}", apiKey.getKeyName());

        // 获取文件ID（优先使用查询参数，兼容请求体）
        String actualFileId = fileId;
        if (actualFileId == null && requestBody != null) {
            actualFileId = (String) requestBody.get("fileId");
        }
        
        if (actualFileId == null || actualFileId.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "文件ID不能为空");
        }

        // 删除文件
        fileService.deleteFile(actualFileId);

        log.info("文件删除回调成功: fileId={}", actualFileId);

        return Result.success();
    }
}
