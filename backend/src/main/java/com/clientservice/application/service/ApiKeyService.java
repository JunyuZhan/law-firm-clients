package com.clientservice.application.service;

import com.clientservice.application.dto.ApiKeyDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.TokenGenerator;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.infrastructure.persistence.mapper.ApiKeyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API密钥服务
 * 
 * <p>API密钥管理与验证服务，提供密钥的CRUD及带缓存的验证功能。</p>
 * 
 * <h3>缓存策略：</h3>
 * <ul>
 *   <li><b>缓存区域</b>：{@code apiKey}</li>
 *   <li><b>缓存键</b>：API密钥值（自动处理Bearer前缀）</li>
 *   <li><b>缓存条件</b>：仅当API密钥值不为null时缓存</li>
 *   <li><b>缓存失效</b>：更新或删除操作时清除所有API密钥缓存（{@code @CacheEvict(allEntries = true)}）</li>
 * </ul>
 * 
 * <h3>缓存失效时机：</h3>
 * <ul>
 *   <li>{@link #updateLastUsedTime(Long)}：更新最后使用时间时不清除缓存（不影响验证结果）</li>
 *   <li>{@link #updateApiKey(Long, ApiKeyDTO)}：更新API密钥时清除所有缓存（影响验证结果）</li>
 *   <li>{@link #deleteApiKey(Long)}：删除API密钥时清除所有缓存</li>
 * </ul>
 * 
 * <p><b>注意：</b>由于缓存键是API密钥值，而更新/删除时无法直接获取旧的API密钥值来精确清除缓存，
 * 因此使用 {@code allEntries = true} 来确保数据一致性。API密钥更新频率低，清除所有缓存的影响较小。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    /**
     * 验证API密钥（带缓存）
     *
     * @param apiKeyValue API密钥值
     * @return API密钥实体
     * @throws BusinessException 如果密钥无效
     */
    @Cacheable(value = "apiKey", key = "#apiKeyValue != null ? ((#apiKeyValue.startsWith('Bearer ')) ? #apiKeyValue.substring(7) : #apiKeyValue) : 'null'", condition = "#apiKeyValue != null", unless = "#result == null")
    public ApiKey validateApiKey(final String apiKeyValue) {
        if (apiKeyValue == null || apiKeyValue.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "API密钥不能为空");
        }

        // 移除Bearer前缀（如果有）
        String key = apiKeyValue.startsWith("Bearer ") 
            ? apiKeyValue.substring(7) 
            : apiKeyValue;

        // 安全修复：日志中只记录密钥前4位，避免完整密钥泄露
        String maskedKey = key.length() > 4 ? key.substring(0, 4) + "****" : "****";

        // 速率限制检查（防止暴力枚举）
        // 使用密钥的哈希作为限制键，避免明文密钥作为Redis键
        String keyHash = org.springframework.util.DigestUtils.md5DigestAsHex(key.getBytes());
        String lockKey = "apikey:fail:" + keyHash;
        try {
            // 限制：每分钟最多允许5次失败尝试，锁定30分钟
            // 注意：RateLimitUtil.checkIpLockedOrLimited是静态方法，但需要redisTemplate参数
            // 这里我们使用RateLimitUtil来检查是否锁定
            com.clientservice.common.util.RateLimitUtil.checkIpLockedOrLimited(redisTemplate, lockKey, maskedKey, "API密钥验证");
        } catch (BusinessException e) {
            log.warn("API密钥验证尝试过于频繁，已锁定: {}", maskedKey);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "API密钥验证尝试过于频繁，请稍后再试");
        }

        ApiKey apiKey = apiKeyMapper.selectByApiKey(key);
        
        if (apiKey == null) {
            log.warn("无效的API密钥: {}", maskedKey);
            // 记录失败尝试并锁定
            handleFailedAttempt(lockKey, maskedKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "API密钥无效");
        }

        // 验证成功，清除失败计数（可选，防止正常使用偶尔输错导致锁定）
        redisTemplate.delete(lockKey);

        // 检查是否启用
        if (apiKey.getEnabled() == null || !apiKey.getEnabled()) {
            log.warn("API密钥已禁用: {}", maskedKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "API密钥已禁用");
        }

        // 检查是否过期
        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            log.warn("API密钥已过期: {}", maskedKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "API密钥已过期");
        }

        // 更新最后使用时间
        updateLastUsedTime(apiKey.getId());

        return apiKey;
    }

    /**
     * 处理失败尝试
     * 
     * @param lockKey Redis Key
     * @param maskedKey 掩码后的密钥
     */
    private void handleFailedAttempt(String lockKey, String maskedKey) {
        try {
            Long count = redisTemplate.opsForValue().increment(lockKey);
            if (count != null && count == 1) {
                // 第一次失败，设置过期时间（例如1分钟窗口）
                redisTemplate.expire(lockKey, java.time.Duration.ofMinutes(1));
            }
            
            // 检查是否达到阈值（5次）
            if (count != null && count >= 5) {
                // 锁定30分钟
                long lockTimeMillis = System.currentTimeMillis() + 30 * 60 * 1000;
                redisTemplate.opsForValue().set(lockKey, String.valueOf(lockTimeMillis));
                // 设置Redis Key过期时间略长于锁定时间，确保锁定过期后Key自动删除
                redisTemplate.expire(lockKey, java.time.Duration.ofMinutes(31));
                log.warn("API密钥验证失败次数过多，已锁定: key={}, count={}", maskedKey, count);
            }
        } catch (Exception e) {
            log.error("记录API密钥验证失败次数异常", e);
        }
    }

    /**
     * 更新最后使用时间（不清除缓存）
     * 
     * <p><b>注意：</b>最后使用时间的更新不影响API密钥验证结果，因此不需要清除缓存。
     * 这样可以避免频繁的缓存清除操作，提升性能。
     *
     * @param id API密钥ID
     */
    @Transactional
    public void updateLastUsedTime(final Long id) {
        apiKeyMapper.updateLastUsedAt(id);
    }

    /**
     * 获取API密钥列表
     *
     * @param enabled 是否启用（可选）
     * @param lawFirmName 律所名称（可选，模糊查询）
     * @param limit 限制数量（可选，默认100）
     * @return API密钥列表
     */
    public List<ApiKeyDTO> getApiKeyList(
            final Boolean enabled,
            final String lawFirmName,
            final Integer limit) {

        LambdaQueryWrapper<ApiKey> queryWrapper = new LambdaQueryWrapper<>();

        if (enabled != null) {
            queryWrapper.eq(ApiKey::getEnabled, enabled);
        }

        if (lawFirmName != null && !lawFirmName.isEmpty()) {
            queryWrapper.like(ApiKey::getLawFirmName, lawFirmName);
        }

        queryWrapper.orderByDesc(ApiKey::getCreatedAt);

        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<ApiKey> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能

        List<ApiKey> apiKeys = apiKeyMapper.selectPage(page, queryWrapper).getRecords();

        return apiKeys.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取API密钥
     *
     * @param id API密钥ID
     * @return API密钥DTO
     */
    public ApiKeyDTO getApiKeyById(final Long id) {
        ApiKey apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null || apiKey.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "API密钥不存在");
        }
        return convertToDTO(apiKey);
    }

    /**
     * 创建API密钥
     *
     * @param keyName 密钥名称
     * @param lawFirmName 律所名称
     * @param expiresAt 过期时间（可选）
     * @return API密钥DTO
     */
    @Transactional
    public ApiKeyDTO createApiKey(
            final String keyName,
            final String lawFirmName,
            final LocalDateTime expiresAt) {

        // 生成API密钥和密钥Secret
        String apiKeyValue = TokenGenerator.generateToken(32);
        String apiSecretValue = TokenGenerator.generateToken(64);

        ApiKey apiKey = ApiKey.builder()
                .keyName(keyName)
                .apiKey(apiKeyValue)
                .apiSecret(apiSecretValue)
                .lawFirmName(lawFirmName)
                .enabled(true)
                .expiresAt(expiresAt)
                .build();

        apiKeyMapper.insert(apiKey);

        log.info("创建API密钥成功: id={}, keyName={}", apiKey.getId(), keyName);

        // 创建时返回完整密钥
        return convertToDTO(apiKey, true);
    }

    /**
     * 更新API密钥（清除缓存）
     *
     * @param id API密钥ID
     * @param keyName 密钥名称（可选）
     * @param lawFirmName 律所名称（可选）
     * @param enabled 是否启用（可选）
     * @param expiresAt 过期时间（可选）
     * @return API密钥DTO
     */
    @Transactional
    @CacheEvict(value = "apiKey", allEntries = true)
    public ApiKeyDTO updateApiKey(
            final Long id,
            final String keyName,
            final String lawFirmName,
            final Boolean enabled,
            final LocalDateTime expiresAt) {

        ApiKey apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null || apiKey.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "API密钥不存在");
        }

        if (keyName != null && !keyName.isEmpty()) {
            apiKey.setKeyName(keyName);
        }
        if (lawFirmName != null && !lawFirmName.isEmpty()) {
            apiKey.setLawFirmName(lawFirmName);
        }
        if (enabled != null) {
            apiKey.setEnabled(enabled);
        }
        if (expiresAt != null) {
            apiKey.setExpiresAt(expiresAt);
        }

        apiKeyMapper.updateById(apiKey);

        log.info("更新API密钥成功: id={}", id);

        return convertToDTO(apiKey);
    }

    /**
     * 删除API密钥（逻辑删除，清除缓存）
     *
     * @param id API密钥ID
     */
    @Transactional
    @CacheEvict(value = "apiKey", allEntries = true)
    public void deleteApiKey(final Long id) {
        ApiKey apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null || apiKey.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "API密钥不存在");
        }

        apiKey.setDeleted(true);
        apiKeyMapper.updateById(apiKey);

        log.info("删除API密钥成功: id={}", id);
    }

    /**
     * 转换为DTO
     *
     * @param apiKey API密钥实体
     * @param showFullKey 是否显示完整密钥（创建时使用）
     */
    private ApiKeyDTO convertToDTO(final ApiKey apiKey, final boolean showFullKey) {
        // 部分隐藏API密钥（只显示前8位和后4位），除非是创建时
        String maskedApiKey = showFullKey ? apiKey.getApiKey() : maskApiKey(apiKey.getApiKey());
        String maskedApiSecret = apiKey.getApiSecret() != null 
                ? (showFullKey ? apiKey.getApiSecret() : maskApiKey(apiKey.getApiSecret())) 
                : null;

        return ApiKeyDTO.builder()
                .id(apiKey.getId())
                .keyName(apiKey.getKeyName())
                .apiKey(maskedApiKey)
                .apiSecret(maskedApiSecret)
                .lawFirmName(apiKey.getLawFirmName())
                .enabled(apiKey.getEnabled())
                .expiresAt(apiKey.getExpiresAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .createdAt(apiKey.getCreatedAt())
                .updatedAt(apiKey.getUpdatedAt())
                .build();
    }

    /**
     * 转换为DTO（默认隐藏密钥）
     */
    private ApiKeyDTO convertToDTO(final ApiKey apiKey) {
        return convertToDTO(apiKey, false);
    }

    /**
     * 隐藏API密钥（只显示前8位和后4位）
     */
    private String maskApiKey(final String apiKey) {
        if (apiKey == null || apiKey.length() <= 12) {
            return "****";
        }
        return apiKey.substring(0, 8) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
