package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知记录Mapper
 */
@Mapper
public interface NotificationRecordMapper extends BaseMapper<NotificationRecord> {

    /**
     * 根据项目ID查询通知记录
     *
     * @param matterId 项目ID
     * @return 通知记录列表
     */
    List<NotificationRecord> selectByMatterId(@Param("matterId") String matterId);

    /**
     * 根据客户ID查询通知记录
     *
     * @param clientId 客户ID
     * @return 通知记录列表
     */
    List<NotificationRecord> selectByClientId(@Param("clientId") Long clientId);
}
