package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.DownloadLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件下载日志Mapper
 */
@Mapper
public interface DownloadLogMapper extends BaseMapper<DownloadLog> {
}
