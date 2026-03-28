package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 访问日志Mapper
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {

    /**
     * 删除指定时间之前的访问日志
     *
     * @param cutoffDate 截止日期
     * @return 删除的记录数
     */
    int deleteBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
