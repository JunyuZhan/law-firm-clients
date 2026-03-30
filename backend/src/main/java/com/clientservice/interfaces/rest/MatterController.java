package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.domain.entity.ClientMatter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目数据控制器
 */
@Slf4j
@Tag(name = "项目数据", description = "接收律所管理系统推送的项目数据")
@RestController
@RequestMapping("/api/matter")
@RequiredArgsConstructor
public class MatterController {

    private final MatterService matterService;
    private final ApiKeyService apiKeyService;

    /**
     * 接收项目数据
     *
     * @param authorization 认证头（Bearer Token）
     * @param request 接收请求
     * @return 响应（包含项目ID和访问链接）
     */
    @Operation(summary = "接收项目数据", description = "接收律所管理系统推送的项目数据")
    @Timed(value = "api.matter.receive", description = "接收项目数据接口耗时")
    @PostMapping("/receive")
    public Result<MatterReceiveResponse> receiveMatterData(
            @RequestHeader(value = "Authorization") final String authorization,
            @Valid @RequestBody final MatterReceiveRequest request,
            final HttpServletRequest httpRequest) {

        // 1. 验证API密钥
        ApiKey apiKey = apiKeyService.validateApiKey(authorization);
        log.info("API密钥验证成功: keyName={}, lawFirmName={}", 
                apiKey.getKeyName(), apiKey.getLawFirmName());

        // 2. 接收项目数据（传递请求对象以支持动态获取 baseUrl）
        MatterReceiveResponse response = matterService.receiveMatterData(request, httpRequest, apiKey);

        log.info("接收项目数据成功: id={}, clientId={}", response.getId(), request.getClientId());

        return Result.success(response);
    }

    /**
     * 获取项目详情（内部API，供律所系统调用）
     *
     * @param authorization 认证头（Bearer Token）
     * @param id 项目ID
     * @return 项目数据
     */
    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详情（内部API）")
    @Timed(value = "api.matter.getById", description = "获取项目详情接口耗时")
    @GetMapping("/{id}")
    public Result<ClientMatter> getMatterById(
            @RequestHeader(value = "Authorization") final String authorization,
            @Parameter(description = "项目ID", required = true) @PathVariable final String id) {

        // 验证API密钥
        ApiKey apiKey = apiKeyService.validateApiKey(authorization);
        log.info("API密钥验证成功: keyName={}", apiKey.getKeyName());

        // 获取项目详情
        ClientMatter matter = matterService.getMatterById(id);

        return Result.success(matter);
    }
}
