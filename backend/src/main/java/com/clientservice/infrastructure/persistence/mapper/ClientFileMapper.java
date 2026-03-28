package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.ClientFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户文件Mapper
 */
@Mapper
public interface ClientFileMapper extends BaseMapper<ClientFile> {

    /**
     * 根据项目ID和文件哈希值查询文件（用于幂等性校验）
     */
    @Select("SELECT * FROM client_file WHERE matter_id = #{matterId} AND file_hash = #{fileHash} AND status = 'ACTIVE' LIMIT 1")
    ClientFile selectByMatterIdAndFileHash(@Param("matterId") String matterId, @Param("fileHash") String fileHash);

    /**
     * 根据项目ID查询文件列表
     *
     * @param matterId 项目ID
     * @param status 状态（可选）
     * @return 文件列表
     */
    List<ClientFile> selectByMatterId(@Param("matterId") String matterId, @Param("status") String status);

    /**
     * 根据外部文件ID查询
     *
     * @param externalFileId 外部文件ID
     * @return 文件实体
     */
    ClientFile selectByExternalFileId(@Param("externalFileId") String externalFileId);

    /**
     * 分页查询文件列表
     */
    List<ClientFile> selectWithPagination(
            @Param("matterId") String matterId,
            @Param("status") String status,
            @Param("fileCategory") String fileCategory,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计文件数量
     */
    Long countWithFilter(
            @Param("matterId") String matterId,
            @Param("status") String status,
            @Param("fileCategory") String fileCategory,
            @Param("keyword") String keyword);

    /**
     * 统计文件大小总和
     */
    Long sumFileSize(@Param("status") String status);

    /**
     * 按类别统计文件数量
     */
    Long countByCategory(@Param("fileCategory") String fileCategory);

    /**
     * 统计有文件的项目数量
     */
    Long countDistinctMatters();

    /**
     * 查询指定时间之前删除的文件
     */
    List<ClientFile> selectDeletedBefore(@Param("before") LocalDateTime before);
}
