package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationTemplateDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationTemplate;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateMapper templateMapper;

    /**
     * 获取模板列表（带缓存）
     *
     * @param templateType 模板类型（可选）
     * @param provider 提供商（可选）
     * @param enabled 是否启用（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 模板列表
     */
    @Cacheable(value = "notificationTemplate", key = "'list:' + (#templateType != null ? #templateType : 'all') + ':' + (#provider != null ? #provider : 'all') + ':' + (#enabled != null ? #enabled : 'all')")
    public List<NotificationTemplateDTO> getTemplateList(
            final String templateType,
            final String provider,
            final Boolean enabled,
            final Integer limit) {

        LambdaQueryWrapper<NotificationTemplate> queryWrapper = new LambdaQueryWrapper<>();

        if (templateType != null && !templateType.isEmpty()) {
            queryWrapper.eq(NotificationTemplate::getTemplateType, templateType);
        }

        if (provider != null && !provider.isEmpty()) {
            queryWrapper.eq(NotificationTemplate::getProvider, provider);
        }

        if (enabled != null) {
            queryWrapper.eq(NotificationTemplate::getEnabled, enabled);
        }

        queryWrapper.orderByDesc(NotificationTemplate::getCreatedAt);

        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<NotificationTemplate> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能

        List<NotificationTemplate> templates = templateMapper.selectPage(page, queryWrapper).getRecords();

        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取模板（带缓存）
     *
     * @param id 模板ID
     * @return 模板DTO
     */
    @Cacheable(value = "notificationTemplate", key = "'id:' + #id", unless = "#result == null")
    public NotificationTemplateDTO getTemplateById(final Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "模板不存在");
        }
        return convertToDTO(template);
    }

    /**
     * 创建模板（清除缓存）
     *
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param templateCode 模板代码
     * @param templateContent 模板内容
     * @param templateVariables 模板变量说明
     * @param provider 提供商
     * @param signName 签名名称
     * @param enabled 是否启用
     * @param description 描述
     * @return 模板DTO
     */
    @Transactional
    @CacheEvict(value = "notificationTemplate", allEntries = true)
    public NotificationTemplateDTO createTemplate(
            final String templateName,
            final String templateType,
            final String templateCode,
            final String templateContent,
            final String templateVariables,
            final String provider,
            final String signName,
            final Boolean enabled,
            final String description) {

        if (templateName == null || templateName.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板名称不能为空");
        }
        if (templateType == null || templateType.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "模板类型不能为空");
        }

        // 检查模板代码是否已存在
        if (templateCode != null && !templateCode.isEmpty() && provider != null && !provider.isEmpty()) {
            NotificationTemplate existing = templateMapper.selectByTemplateCode(templateCode, provider);
            if (existing != null && !existing.getDeleted()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "该模板代码已存在");
            }
        }

        NotificationTemplate template = NotificationTemplate.builder()
                .templateName(templateName)
                .templateType(templateType)
                .templateCode(templateCode)
                .templateContent(templateContent)
                .templateVariables(templateVariables)
                .provider(provider)
                .signName(signName)
                .enabled(enabled != null ? enabled : true)
                .description(description)
                .build();

        templateMapper.insert(template);

        log.info("创建通知模板成功: id={}, templateName={}, type={}", 
                template.getId(), templateName, templateType);

        return convertToDTO(template);
    }

    /**
     * 更新模板（清除缓存）
     *
     * @param id 模板ID
     * @param templateName 模板名称（可选）
     * @param templateContent 模板内容（可选）
     * @param templateVariables 模板变量说明（可选）
     * @param enabled 是否启用（可选）
     * @param description 描述（可选）
     * @return 模板DTO
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "notificationTemplate", key = "'id:' + #id"),
        @CacheEvict(value = "notificationTemplate", allEntries = true)
    })
    public NotificationTemplateDTO updateTemplate(
            final Long id,
            final String templateName,
            final String templateContent,
            final String templateVariables,
            final Boolean enabled,
            final String description) {

        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "模板不存在");
        }

        if (templateName != null && !templateName.isEmpty()) {
            template.setTemplateName(templateName);
        }
        if (templateContent != null) {
            template.setTemplateContent(templateContent);
        }
        if (templateVariables != null) {
            template.setTemplateVariables(templateVariables);
        }
        if (enabled != null) {
            template.setEnabled(enabled);
        }
        if (description != null) {
            template.setDescription(description);
        }

        templateMapper.updateById(template);

        log.info("更新通知模板成功: id={}, templateName={}", id, template.getTemplateName());

        return convertToDTO(template);
    }

    /**
     * 删除模板（逻辑删除，清除缓存）
     *
     * @param id 模板ID
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "notificationTemplate", key = "'id:' + #id"),
        @CacheEvict(value = "notificationTemplate", allEntries = true)
    })
    public void deleteTemplate(final Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "模板不存在");
        }

        template.setDeleted(true);
        templateMapper.updateById(template);

        log.info("删除通知模板成功: id={}, templateName={}", id, template.getTemplateName());
    }

    /**
     * 根据类型和提供商获取启用的模板（带缓存）
     *
     * @param templateType 模板类型
     * @param provider 提供商
     * @return 模板列表
     */
    @Cacheable(value = "notificationTemplate", key = "'enabled:' + #templateType + ':' + #provider")
    public List<NotificationTemplateDTO> getEnabledTemplatesByTypeAndProvider(
            final String templateType,
            final String provider) {

        List<NotificationTemplate> templates = templateMapper.selectEnabledByTypeAndProvider(templateType, provider);

        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private NotificationTemplateDTO convertToDTO(final NotificationTemplate template) {
        return NotificationTemplateDTO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .templateType(template.getTemplateType())
                .templateCode(template.getTemplateCode())
                .templateContent(template.getTemplateContent())
                .templateVariables(template.getTemplateVariables())
                .provider(template.getProvider())
                .signName(template.getSignName())
                .enabled(template.getEnabled())
                .description(template.getDescription())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
