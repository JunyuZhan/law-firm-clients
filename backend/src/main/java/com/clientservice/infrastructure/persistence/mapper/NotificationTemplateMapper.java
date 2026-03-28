package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知模板Mapper
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {

    /**
     * 根据模板类型和提供商查询启用的模板
     *
     * @param templateType 模板类型
     * @param provider 提供商
     * @return 模板列表
     */
    List<NotificationTemplate> selectEnabledByTypeAndProvider(
            @Param("templateType") String templateType,
            @Param("provider") String provider);

    /**
     * 根据模板代码查询
     *
     * @param templateCode 模板代码
     * @param provider 提供商
     * @return 模板
     */
    NotificationTemplate selectByTemplateCode(
            @Param("templateCode") String templateCode,
            @Param("provider") String provider);
}
