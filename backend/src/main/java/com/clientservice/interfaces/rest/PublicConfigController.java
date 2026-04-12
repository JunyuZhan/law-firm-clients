package com.clientservice.interfaces.rest;

import com.clientservice.application.service.SysConfigService;
import com.clientservice.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 公开配置查询控制器（无需认证）
 * 
 * <p>提供客户门户等公开页面需要的配置信息，如 ICP 备案号等
 */
@Slf4j
@Tag(name = "公开配置", description = "公开配置查询接口（无需认证）")
@RestController
@RequestMapping("/api/public/config")
@RequiredArgsConstructor
public class PublicConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 允许公开访问的配置键白名单
     * 只有在白名单中的配置才能被公开查询
     */
    private static final Set<String> PUBLIC_CONFIG_KEYS = Set.of(
            // 门户页面配置
            "system.icp-license",       // ICP备案号
            "system.law-firm-name",     // 律所名称
            "system.law-firm-website",  // 律所官网
            "system.app-slogan",        // 首页标语
            "system.copyright",         // 版权信息
            "system.portal-eyebrow-en", // 门户页英文眉标（可选）
            "system.portal-access-notice", // 门户页客户说明（公开页主文案）
            "system.staff-entry-label", // 门户页工作人员入口按钮文案
            // 系统品牌配置
            "system.app-name",          // 系统名称（浏览器标题）
            "system.app-short-name",    // 系统简称（管理后台侧边栏）
            "system.app-short-name-en", // 系统英文简称
            "system.logo-url"           // Logo图片地址
    );

    /**
     * 获取公开配置
     * 
     * @param keys 配置键列表，多个用逗号分隔（可选，不传则返回所有公开配置）
     * @return 配置键值对
     */
    @Operation(summary = "获取公开配置", description = "获取允许公开访问的系统配置（无需认证）")
    @GetMapping
    public Result<Map<String, String>> getPublicConfig(
            @Parameter(description = "配置键列表，多个用逗号分隔") 
            @RequestParam(required = false) final String keys) {

        Map<String, String> result = new HashMap<>();

        if (keys != null && !keys.isEmpty()) {
            // 只返回请求的且在白名单中的配置
            String[] requestedKeys = keys.split(",");
            for (String key : requestedKeys) {
                String trimmedKey = key.trim();
                if (PUBLIC_CONFIG_KEYS.contains(trimmedKey)) {
                    String value = sysConfigService.getConfigValue(trimmedKey, "");
                    result.put(trimmedKey, value);
                }
            }
        } else {
            // 返回所有公开配置
            for (String key : PUBLIC_CONFIG_KEYS) {
                String value = sysConfigService.getConfigValue(key, "");
                result.put(key, value);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取 ICP 备案号（便捷接口）
     * 
     * @return ICP 备案号
     */
    @Operation(summary = "获取ICP备案号", description = "获取ICP备案号配置（无需认证）")
    @GetMapping("/icp")
    public Result<String> getIcpLicense() {
        String icpLicense = sysConfigService.getConfigValue("system.icp-license", "");
        return Result.success(icpLicense);
    }

    /**
     * 获取门户页面配置（便捷接口）
     * 
     * <p>一次性返回门户页面需要的所有配置
     * 
     * @return 门户配置
     */
    @Operation(summary = "获取门户配置", description = "获取客户门户页面所需的所有配置（无需认证）")
    @GetMapping("/portal")
    public Result<Map<String, String>> getPortalConfig() {
        Map<String, String> result = new HashMap<>();
        
        // 律所名称
        result.put("lawFirmName", sysConfigService.getConfigValue("system.law-firm-name", ""));
        // 律所官网
        result.put("lawFirmWebsite", sysConfigService.getConfigValue("system.law-firm-website", ""));
        // 首页标语
        result.put("appSlogan", sysConfigService.getConfigValue("system.app-slogan", ""));
        // ICP备案号
        result.put("icpLicense", sysConfigService.getConfigValue("system.icp-license", ""));
        // 版权信息
        result.put("copyright", sysConfigService.getConfigValue("system.copyright", ""));
        // Logo地址
        result.put("logoUrl", sysConfigService.getConfigValue("system.logo-url", ""));
        // 门户公开落地页文案
        result.put("portalEyebrowEn", sysConfigService.getConfigValue("system.portal-eyebrow-en", ""));
        result.put("portalAccessNotice", sysConfigService.getConfigValue("system.portal-access-notice", ""));
        result.put("staffEntryLabel", sysConfigService.getConfigValue("system.staff-entry-label", ""));

        return Result.success(result);
    }

    /**
     * 获取系统品牌配置（便捷接口）
     * 
     * <p>返回系统品牌相关配置，用于管理后台侧边栏、浏览器标题等
     * 
     * @return 品牌配置
     */
    @Operation(summary = "获取系统品牌配置", description = "获取系统名称、简称、Logo等品牌配置（无需认证）")
    @GetMapping("/brand")
    public Result<Map<String, String>> getBrandConfig() {
        Map<String, String> result = new HashMap<>();
        
        // 系统名称（浏览器标题）
        result.put("appName", sysConfigService.getConfigValue("system.app-name", ""));
        // 系统简称（管理后台侧边栏）
        result.put("appShortName", sysConfigService.getConfigValue("system.app-short-name", ""));
        // 系统英文简称
        result.put("appShortNameEn", sysConfigService.getConfigValue("system.app-short-name-en", ""));
        // Logo地址
        result.put("logoUrl", sysConfigService.getConfigValue("system.logo-url", ""));
        
        return Result.success(result);
    }
}
