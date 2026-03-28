package com.clientservice.interfaces.rest;

import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Slf4j
@Tag(name = "健康检查", description = "系统健康检查接口")
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @Operation(summary = "健康检查", description = "检查系统健康状态")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "客户服务系统");
        health.put("version", "1.0.0");
        
        return Result.success(health);
    }

    /**
     * 就绪检查
     *
     * @return 就绪状态
     */
    @Operation(summary = "就绪检查", description = "检查系统是否就绪")
    @GetMapping("/ready")
    public Result<Map<String, Object>> ready() {
        Map<String, Object> ready = new HashMap<>();
        ready.put("status", "READY");
        ready.put("timestamp", LocalDateTime.now());
        
        return Result.success(ready);
    }

    /**
     * 存活检查
     *
     * @return 存活状态
     */
    @Operation(summary = "存活检查", description = "检查系统是否存活")
    @GetMapping("/live")
    public Result<Map<String, Object>> live() {
        Map<String, Object> live = new HashMap<>();
        live.put("status", "ALIVE");
        live.put("timestamp", LocalDateTime.now());
        
        return Result.success(live);
    }
}
