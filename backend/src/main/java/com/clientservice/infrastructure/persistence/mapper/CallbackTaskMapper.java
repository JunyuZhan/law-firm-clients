package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.CallbackTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回调出站任务 Mapper。
 */
@Mapper
public interface CallbackTaskMapper extends BaseMapper<CallbackTask> {
}
