package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.AdminLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员登录日志Mapper
 */
@Mapper
public interface AdminLoginLogMapper extends BaseMapper<AdminLoginLog> {
}
