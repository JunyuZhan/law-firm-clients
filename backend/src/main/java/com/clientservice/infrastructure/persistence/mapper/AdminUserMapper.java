package com.clientservice.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clientservice.domain.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员用户Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 根据用户名查询管理员用户
     *
     * @param username 用户名
     * @return 管理员用户
     */
    AdminUser selectByUsername(@Param("username") String username);

    /**
     * 更新最后登录时间和IP
     *
     * @param id 用户ID
     * @param ipAddress IP地址
     */
    void updateLastLoginTime(@Param("id") Long id, @Param("ipAddress") String ipAddress);

    /**
     * 增加登录失败次数
     *
     * @param id 用户ID
     */
    void incrementFailedLoginCount(@Param("id") Long id);

    /**
     * 重置登录失败次数
     *
     * @param id 用户ID
     */
    void resetFailedLoginCount(@Param("id") Long id);

    /**
     * 锁定账户
     *
     * @param id 用户ID
     * @param lockedUntil 锁定到期时间
     */
    void lockAccount(@Param("id") Long id, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);

    /**
     * 解锁账户
     *
     * @param id 用户ID
     */
    void unlockAccount(@Param("id") Long id);
}
