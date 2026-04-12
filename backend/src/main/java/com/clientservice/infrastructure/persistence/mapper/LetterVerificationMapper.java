package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.LetterVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 函件验证Mapper
 */
@Mapper
public interface LetterVerificationMapper extends BaseMapper<LetterVerification> {

    /**
     * 根据函件申请编号查询
     *
     * @param applicationNo 函件申请编号
     * @return 函件验证实体
     */
    @Select("SELECT * FROM letter_verification WHERE application_no = #{applicationNo} AND deleted = false")
    LetterVerification selectByApplicationNo(@Param("applicationNo") String applicationNo);

    /**
     * 根据来源 API Key 与函件申请编号查询
     */
    @Select("""
            SELECT * FROM letter_verification
            WHERE application_no = #{applicationNo}
              AND source_api_key_id = #{sourceApiKeyId}
              AND deleted = false
            LIMIT 1
            """)
    LetterVerification selectByApplicationNoAndSource(
            @Param("applicationNo") String applicationNo,
            @Param("sourceApiKeyId") Long sourceApiKeyId);

    /**
     * 根据律所系统函件ID查询
     *
     * @param letterId 律所系统函件ID
     * @return 函件验证实体
     */
    @Select("SELECT * FROM letter_verification WHERE letter_id = #{letterId} AND deleted = false")
    LetterVerification selectByLetterId(@Param("letterId") Long letterId);

    /**
     * 根据来源 API Key 与律所系统函件ID查询
     */
    @Select("""
            SELECT * FROM letter_verification
            WHERE letter_id = #{letterId}
              AND source_api_key_id = #{sourceApiKeyId}
              AND deleted = false
            LIMIT 1
            """)
    LetterVerification selectByLetterIdAndSource(
            @Param("letterId") Long letterId,
            @Param("sourceApiKeyId") Long sourceApiKeyId);

    /**
     * 根据验证码查询
     *
     * @param verificationCode 验证码
     * @return 函件验证实体
     */
    @Select("SELECT * FROM letter_verification WHERE verification_code = #{verificationCode} AND deleted = false")
    LetterVerification selectByVerificationCode(@Param("verificationCode") String verificationCode);

    /**
     * 更新验证次数和最后验证信息
     *
     * @param id 主键ID
     * @param lastVerifyIp 最后验证IP
     */
    @Update("UPDATE letter_verification SET verify_count = verify_count + 1, last_verify_at = CURRENT_TIMESTAMP, last_verify_ip = #{lastVerifyIp}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateVerifyInfo(@Param("id") Long id, @Param("lastVerifyIp") String lastVerifyIp);

    /**
     * 撤销验证（更新状态为REVOKED）
     *
     * @param letterId 律所系统函件ID
     */
    @Update("UPDATE letter_verification SET status = 'REVOKED', updated_at = CURRENT_TIMESTAMP WHERE letter_id = #{letterId} AND deleted = false")
    void revokeByLetterId(@Param("letterId") Long letterId);

    /**
     * 按来源撤销验证
     */
    @Update("""
            UPDATE letter_verification
            SET status = 'REVOKED', updated_at = CURRENT_TIMESTAMP
            WHERE letter_id = #{letterId}
              AND source_api_key_id = #{sourceApiKeyId}
              AND deleted = false
            """)
    void revokeByLetterIdAndSource(
            @Param("letterId") Long letterId,
            @Param("sourceApiKeyId") Long sourceApiKeyId);
}
