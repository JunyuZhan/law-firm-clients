package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.ApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * API密钥Mapper
 */
@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {

    /**
     * 根据API密钥查询
     *
     * @param apiKey API密钥
     * @return API密钥实体
     */
    ApiKey selectByApiKey(@Param("apiKey") String apiKey);

    /**
     * 更新最后使用时间
     *
     * @param id 主键ID
     * @return 更新行数
     */
    int updateLastUsedAt(@Param("id") Long id);
}
