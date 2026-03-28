package com.clientservice.application.service;

import com.clientservice.application.dto.SysConfigDTO;
import com.clientservice.application.event.ConfigUpdateEvent;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.SysConfig;
import com.clientservice.infrastructure.persistence.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置服务
 * 
 * <h3>缓存策略：</h3>
 * <ul>
 *   <li><b>缓存区域</b>：{@code sysConfig}</li>
 *   <li><b>缓存键</b>：配置键（{@code configKey}）</li>
 *   <li><b>缓存条件</b>：仅当配置值不为null时缓存</li>
 *   <li><b>缓存过期时间</b>：2小时（在 {@code CacheConfig} 中配置）</li>
 *   <li><b>缓存失效</b>：更新或删除配置时精确清除对应配置的缓存（按配置键）</li>
 * </ul>
 * 
 * <h3>缓存失效时机：</h3>
 * <ul>
 *   <li>{@link #saveConfig(String, String, String)}：保存配置时清除对应配置的缓存</li>
 *   <li>{@link #updateConfig(Long, String, String)}：更新配置时清除对应配置的缓存</li>
 *   <li>{@link #deleteConfig(Long)}：删除配置时清除对应配置的缓存</li>
 * </ul>
 * 
 * <p><b>注意：</b>系统配置读取频率高，但更新频率低。使用精确清除（按配置键）可以避免清除其他未变更的配置缓存，
 * 提升缓存命中率。只有在配置项很多且更新频繁时，才需要考虑使用 {@code allEntries = true}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper configMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheManager cacheManager;

    /**
     * 获取配置值（带缓存）
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Cacheable(value = "sysConfig", key = "#configKey", unless = "#result == null")
    public String getConfigValue(final String configKey) {
        SysConfig config = configMapper.selectByConfigKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置值（带默认值）
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(final String configKey, final String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public boolean getBooleanConfig(final String configKey, final boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 获取整数配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 整数值
     */
    public int getIntConfig(final String configKey, final int defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值不是整数: configKey={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    /**
     * 获取配置列表
     *
     * @param configKey 配置键（可选，模糊查询）
     * @param configType 配置类型（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 配置列表
     */
    public List<SysConfigDTO> getConfigList(
            final String configKey,
            final String configType,
            final Integer limit) {

        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();

        if (configKey != null && !configKey.isEmpty()) {
            queryWrapper.like(SysConfig::getConfigKey, configKey);
        }

        if (configType != null && !configType.isEmpty()) {
            queryWrapper.eq(SysConfig::getConfigType, configType);
        }

        queryWrapper.orderByAsc(SysConfig::getConfigKey);

        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<SysConfig> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能

        List<SysConfig> configs = configMapper.selectPage(page, queryWrapper).getRecords();

        return configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取配置
     *
     * @param id 配置ID
     * @return 配置DTO
     */
    public SysConfigDTO getConfigById(final Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null || config.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }
        return convertToDTO(config);
    }

    /**
     * 创建或更新配置（清除缓存）
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @param configType 配置类型
     * @param description 描述
     * @return 配置DTO
     */
    @Transactional
    @CacheEvict(value = "sysConfig", key = "#configKey")
    public SysConfigDTO saveConfig(
            final String configKey,
            final String configValue,
            final String configType,
            final String description) {

        if (configKey == null || configKey.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置键不能为空");
        }

        // 检查是否已存在
        SysConfig existing = configMapper.selectByConfigKey(configKey);
        
        if (existing != null && !existing.getDeleted()) {
            // 更新现有配置
            existing.setConfigValue(configValue);
            if (configType != null && !configType.isEmpty()) {
                existing.setConfigType(configType);
            }
            if (description != null) {
                existing.setDescription(description);
            }
            configMapper.updateById(existing);
            
            // 精确清除该配置的缓存（按配置键）
            clearConfigCache(configKey);
            
            log.info("更新系统配置: configKey={}", configKey);
            
            // 发布配置更新事件
            eventPublisher.publishEvent(new ConfigUpdateEvent(
                    this, configKey, configValue, ConfigUpdateEvent.EventType.UPDATE));
            
            return convertToDTO(existing);
        } else {
            // 创建新配置
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue(configValue)
                    .configType(configType != null ? configType : "STRING")
                    .description(description)
                    .build();
            configMapper.insert(config);
            
            // 精确清除该配置的缓存（按配置键，虽然新创建但可能已有缓存）
            clearConfigCache(configKey);
            
            log.info("创建系统配置: configKey={}", configKey);
            
            // 发布配置创建事件
            eventPublisher.publishEvent(new ConfigUpdateEvent(
                    this, configKey, configValue, ConfigUpdateEvent.EventType.CREATE));
            
            return convertToDTO(config);
        }
    }

    /**
     * 更新配置（清除缓存）
     *
     * @param id 配置ID
     * @param configValue 配置值（可选）
     * @param configType 配置类型（可选）
     * @param description 描述（可选）
     * @return 配置DTO
     */
    @Transactional
    public SysConfigDTO updateConfig(
            final Long id,
            final String configValue,
            final String configType,
            final String description) {

        SysConfig config = configMapper.selectById(id);
        if (config == null || config.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }

        String configKey = config.getConfigKey(); // 保存配置键，用于清除缓存

        if (configValue != null) {
            config.setConfigValue(configValue);
        }
        if (configType != null && !configType.isEmpty()) {
            config.setConfigType(configType);
        }
        if (description != null) {
            config.setDescription(description);
        }

        configMapper.updateById(config);
        
        // 精确清除该配置的缓存（按配置键）
        clearConfigCache(configKey);
        
        log.info("更新系统配置: id={}, configKey={}", id, configKey);
        
        // 发布配置更新事件
        eventPublisher.publishEvent(new ConfigUpdateEvent(
                this, configKey, config.getConfigValue(), ConfigUpdateEvent.EventType.UPDATE));
        
        return convertToDTO(config);
    }

    /**
     * 删除配置（逻辑删除，清除缓存）
     *
     * @param id 配置ID
     */
    @Transactional
    public void deleteConfig(final Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null || config.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }

        String configKey = config.getConfigKey();
        
        // 精确清除该配置的缓存（按配置键）
        clearConfigCache(configKey);
        String configValue = config.getConfigValue();
        config.setDeleted(true);
        configMapper.updateById(config);

        log.info("删除系统配置: id={}, configKey={}", id, configKey);

        // 发布配置删除事件
        eventPublisher.publishEvent(new ConfigUpdateEvent(
                this, configKey, configValue, ConfigUpdateEvent.EventType.DELETE));
    }

    /**
     * 清除配置缓存（按配置键）
     *
     * @param configKey 配置键
     */
    private void clearConfigCache(final String configKey) {
        try {
            org.springframework.cache.Cache cache = cacheManager.getCache("sysConfig");
            if (cache != null) {
                cache.evict(configKey);
                log.debug("清除配置缓存: configKey={}", configKey);
            }
        } catch (Exception e) {
            log.warn("清除配置缓存失败: configKey={}", configKey, e);
        }
    }

    /**
     * 转换为DTO
     */
    private SysConfigDTO convertToDTO(final SysConfig config) {
        return SysConfigDTO.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .configType(config.getConfigType())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
