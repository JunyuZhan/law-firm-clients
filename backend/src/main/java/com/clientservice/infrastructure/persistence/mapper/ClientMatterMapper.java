package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.ClientMatter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目数据Mapper
 */
@Mapper
public interface ClientMatterMapper extends BaseMapper<ClientMatter> {

    /**
     * 根据律所系统项目ID查询
     *
     * @param lawFirmMatterId 律所系统项目ID
     * @return 项目数据
     */
    ClientMatter selectByLawFirmMatterId(@Param("lawFirmMatterId") Long lawFirmMatterId);

    /**
     * 根据访问令牌查询
     *
     * @param accessToken 访问令牌
     * @return 项目数据
     */
    ClientMatter selectByAccessToken(@Param("accessToken") String accessToken);

    /**
     * 更新状态
     *
     * @param id 项目ID
     * @param status 状态
     * @return 更新行数
     */
    int updateStatus(@Param("id") String id, @Param("status") String status);
}
