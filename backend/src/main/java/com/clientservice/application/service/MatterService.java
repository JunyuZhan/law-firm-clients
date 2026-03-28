package com.clientservice.application.service;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.dto.MatterListDTO;
import com.clientservice.application.dto.MatterDetailDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.TokenGenerator;
import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * 项目数据服务
 * 
 * <h3>缓存策略：</h3>
 * <ul>
 *   <li><b>缓存区域</b>：{@code matter}</li>
 *   <li><b>缓存键</b>：
 *     <ul>
 *       <li>按Token查询：{@code "token:{token}"}</li>
 *       <li>按ID查询：{@code "{id}"}</li>
 *     </ul>
 *   </li>
 *   <li><b>缓存过期时间</b>：10分钟（在 {@code CacheConfig} 中配置）</li>
 *   <li><b>缓存失效</b>：撤销或过期项目时清除对应缓存</li>
 * </ul>
 * 
 * <h3>缓存失效时机：</h3>
 * <ul>
 *   <li>{@link #revokeMatter(String)}：撤销项目时清除Token和ID对应的缓存</li>
 *   <li>{@link #expireMatter(String)}：过期项目时清除Token和ID对应的缓存</li>
 * </ul>
 * 
 * <p><b>注意：</b>项目数据更新频率低，但查询频率高，使用缓存可以显著减少数据库查询。
 * 项目撤销或过期时需要及时清除缓存，确保数据一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatterService {

    private final ClientMatterMapper matterMapper;
    private final ObjectMapper objectMapper;
    private final UrlGenerator urlGenerator;
    private final NotificationService notificationService;
    private final CacheManager cacheManager;

    /** 默认有效期（天） */
    @Value("${client-service.api.token-expire-days:30}")
    private int defaultValidDays;

    /** 令牌长度 */
    @Value("${client-service.api.token-length:32}")
    private int tokenLength;

    /**
     * 接收项目数据
     *
     * @param request 接收请求
     * @return 响应（包含项目ID和访问链接）
     */
    @Transactional
    public MatterReceiveResponse receiveMatterData(final MatterReceiveRequest request) {
        return receiveMatterData(request, null);
    }

    /**
     * 接收项目数据（支持从请求上下文动态获取 baseUrl）
     *
     * @param request 接收请求
     * @param httpRequest HTTP请求（可选，用于动态获取 baseUrl）
     * @return 响应（包含项目ID和访问链接）
     */
    @Transactional
    public MatterReceiveResponse receiveMatterData(final MatterReceiveRequest request, final HttpServletRequest httpRequest) {
        try {
            // 1. 提取项目ID
            Long matterId = extractMatterId(request.getMatterData());
            if (matterId == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "项目数据中缺少matterId字段");
            }

            // 2. 检查是否已存在（根据律所系统项目ID）
            ClientMatter existing = matterMapper.selectByLawFirmMatterId(matterId);
            if (existing != null) {
                // 保存旧token（用于清除缓存）
                String oldToken = existing.getAccessToken();
                // 更新现有记录（会重新生成token）
                MatterReceiveResponse response = updateExistingMatter(existing, request, httpRequest);
                // 清除相关缓存（因为token已变化）
                clearMatterCache(existing.getId(), oldToken);
                return response;
            }

            // 3. 创建新记录（使用 try-catch 处理并发插入导致的唯一约束冲突）
            try {
                return createNewMatter(matterId, request, httpRequest);
            } catch (DataIntegrityViolationException e) {
                log.warn("并发插入检测: lawFirmMatterId={} 已存在，转为更新操作", matterId);
                // 重新查询并更新
                existing = matterMapper.selectByLawFirmMatterId(matterId);
                if (existing != null) {
                    // 保存旧token（用于清除缓存）
                    String oldToken = existing.getAccessToken();
                    MatterReceiveResponse response = updateExistingMatter(existing, request, httpRequest);
                    clearMatterCache(existing.getId(), oldToken);
                    return response;
                } else {
                    // 如果重新查询仍为空，说明数据可能被删除，重新抛出异常
                    throw e;
                }
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("接收项目数据失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "接收项目数据失败: " + e.getMessage());
        }
    }

    /**
     * 提取项目ID
     *
     * @param matterData 项目数据
     * @return 项目ID
     */
    private Long extractMatterId(final java.util.Map<String, Object> matterData) {
        if (matterData == null) {
            return null;
        }
        Object matterIdObj = matterData.get("matterId");
        if (matterIdObj == null) {
            return null;
        }
        if (matterIdObj instanceof Number) {
            return ((Number) matterIdObj).longValue();
        }
        try {
            return Long.parseLong(matterIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 创建新项目记录（支持从请求上下文动态获取 baseUrl）
     *
     * @param lawFirmMatterId 律所系统项目ID
     * @param request 接收请求
     * @param httpRequest HTTP请求（可选，用于动态获取 baseUrl）
     * @return 响应
     */
    private MatterReceiveResponse createNewMatter(
            final Long lawFirmMatterId, final MatterReceiveRequest request, final HttpServletRequest httpRequest) {
        // 生成ID和令牌
        String id = TokenGenerator.generateId();
        String token = TokenGenerator.generateToken(tokenLength);

        // 计算过期时间
        int validDays = request.getValidDays() != null ? request.getValidDays() : defaultValidDays;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);

        // 序列化项目数据
        String matterDataJson;
        try {
            matterDataJson = objectMapper.writeValueAsString(request.getMatterData());
        } catch (Exception e) {
            log.error("序列化项目数据失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "序列化项目数据失败");
        }

        // 创建实体
        ClientMatter matter = ClientMatter.builder()
                .id(id)
                .lawFirmMatterId(lawFirmMatterId)
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                .matterData(matterDataJson)
                .scopes(String.join(",", request.getScopes()))
                .validDays(validDays)
                .expiresAt(expiresAt)
                .accessToken(token)
                .status(ClientMatter.STATUS_ACTIVE)
                .build();

        // 生成访问链接（传递请求对象以支持动态获取 baseUrl）
        String accessUrl = urlGenerator.generateAccessUrl(id, token, httpRequest);
        matter.setAccessUrl(accessUrl);

        // 保存到数据库
        matterMapper.insert(matter);

        log.info("接收项目数据成功: matterId={}, clientId={}, id={}", 
                lawFirmMatterId, request.getClientId(), id);

        // 异步发送通知
        notificationService.sendNotificationAsync(matter);

        return MatterReceiveResponse.builder()
                .id(id)
                .accessUrl(accessUrl)
                .build();
    }

    /**
     * 更新现有项目记录（支持从请求上下文动态获取 baseUrl）
     *
     * @param existing 现有记录
     * @param request 接收请求
     * @param httpRequest HTTP请求（可选，用于动态获取 baseUrl）
     * @return 响应
     */
    private MatterReceiveResponse updateExistingMatter(
            final ClientMatter existing, final MatterReceiveRequest request, final HttpServletRequest httpRequest) {
        // 重新生成令牌和访问链接（传递请求对象以支持动态获取 baseUrl）
        String token = TokenGenerator.generateToken(tokenLength);
        String accessUrl = urlGenerator.generateAccessUrl(existing.getId(), token, httpRequest);

        // 计算过期时间
        int validDays = request.getValidDays() != null ? request.getValidDays() : defaultValidDays;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);

        // 序列化项目数据
        String matterDataJson;
        try {
            matterDataJson = objectMapper.writeValueAsString(request.getMatterData());
        } catch (Exception e) {
            log.error("序列化项目数据失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "序列化项目数据失败");
        }

        // 更新实体
        existing.setClientName(request.getClientName());
        existing.setMatterData(matterDataJson);
        existing.setScopes(String.join(",", request.getScopes()));
        existing.setValidDays(validDays);
        existing.setExpiresAt(expiresAt);
        existing.setAccessToken(token);
        existing.setAccessUrl(accessUrl);
        existing.setStatus(ClientMatter.STATUS_ACTIVE);

        // 更新数据库
        matterMapper.updateById(existing);

        log.info("更新项目数据成功: id={}, clientId={}", existing.getId(), request.getClientId());

        // 异步发送通知
        notificationService.sendNotificationAsync(existing);

        return MatterReceiveResponse.builder()
                .id(existing.getId())
                .accessUrl(accessUrl)
                .build();
    }

    /**
     * 根据访问令牌获取项目（带缓存，缓存时间较短因为有过期检查）
     *
     * @param token 访问令牌
     * @return 项目数据
     */
    @Cacheable(value = "matter", key = "'token:' + #token", unless = "#result == null")
    public ClientMatter getMatterByToken(final String token) {
        ClientMatter matter = matterMapper.selectByAccessToken(token);
        if (matter == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在或访问令牌无效");
        }

        // 检查是否过期
        if (matter.getExpiresAt() != null && matter.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 更新状态为已过期
            matterMapper.updateStatus(matter.getId(), ClientMatter.STATUS_EXPIRED);
            throw new BusinessException(ErrorCode.FORBIDDEN, "项目访问已过期");
        }

        return matter;
    }

    /**
     * 根据ID获取项目（带缓存）
     *
     * @param id 项目ID
     * @return 项目数据
     */
    @Cacheable(value = "matter", key = "'id:' + #id", unless = "#result == null")
    public ClientMatter getMatterById(final String id) {
        ClientMatter matter = matterMapper.selectById(id);
        if (matter == null || matter.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }
        return matter;
    }

    /**
     * 撤销项目访问（清除缓存）
     *
     * @param id 项目ID
     */
    @Transactional
    public void revokeMatter(final String id) {
        ClientMatter matter = matterMapper.selectById(id);
        if (matter == null || matter.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }

        if (ClientMatter.STATUS_REVOKED.equals(matter.getStatus())) {
            log.warn("项目已被撤销: id={}", id);
            return;
        }

        // 更新状态为已撤销
        matterMapper.updateStatus(id, ClientMatter.STATUS_REVOKED);

        // 精确清除相关缓存（按ID和token）
        clearMatterCache(id, matter.getAccessToken());

        log.info("项目访问已撤销: id={}, clientId={}", id, matter.getClientId());
    }

    /**
     * 清除项目相关缓存
     *
     * @param id 项目ID
     * @param oldToken 旧token（如果存在）
     */
    private void clearMatterCache(final String id, final String oldToken) {
        try {
            org.springframework.cache.Cache cache = cacheManager.getCache("matter");
            if (cache != null) {
                // 清除按ID的缓存
                cache.evict("id:" + id);
                // 清除按token的缓存（如果token存在）
                if (oldToken != null && !oldToken.isEmpty()) {
                    cache.evict("token:" + oldToken);
                }
                log.debug("清除项目缓存: id={}, token=***", id);
            }
        } catch (Exception e) {
            log.warn("清除项目缓存失败: id={}", id, e);
        }
    }

    /**
     * 处理过期项目（定时任务调用，清除缓存）
     */
    @Transactional
    public void expireMatter(final String id) {
        ClientMatter matter = matterMapper.selectById(id);
        if (matter == null || matter.getDeleted()) {
            return;
        }

        if (ClientMatter.STATUS_EXPIRED.equals(matter.getStatus())) {
            return;
        }

        // 检查是否过期
        if (matter.getExpiresAt() != null && matter.getExpiresAt().isBefore(LocalDateTime.now())) {
            matterMapper.updateStatus(id, ClientMatter.STATUS_EXPIRED);
            
            // 精确清除相关缓存（按ID和token）
            clearMatterCache(id, matter.getAccessToken());
            
            log.info("项目已过期: id={}, clientId={}", id, matter.getClientId());
        }
    }

    /**
     * 获取项目列表（管理后台使用）
     *
     * @param clientId 客户ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量（可选，默认100）
     * @return 项目列表
     */
    public List<MatterListDTO> getMatterList(
            final Long clientId,
            final String status,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final Integer limit) {

        LambdaQueryWrapper<ClientMatter> queryWrapper = new LambdaQueryWrapper<>();

        if (clientId != null) {
            queryWrapper.eq(ClientMatter::getClientId, clientId);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ClientMatter::getStatus, status);
        }

        if (startTime != null) {
            queryWrapper.ge(ClientMatter::getCreatedAt, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(ClientMatter::getCreatedAt, endTime);
        }

        queryWrapper.orderByDesc(ClientMatter::getCreatedAt);

        // 使用 MyBatis-Plus 分页查询，避免 SQL 拼接风险
        int limitValue = limit != null && limit > 0 ? Math.min(limit, 1000) : 100;
        Page<ClientMatter> page = new Page<>(1, limitValue);
        page.setSearchCount(false); // 不需要查询总数，提高性能
        
        List<ClientMatter> matters = matterMapper.selectPage(page, queryWrapper).getRecords();

        return matters.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表DTO
     */
    private MatterListDTO convertToListDTO(final ClientMatter matter) {
        return MatterListDTO.builder()
                .id(matter.getId())
                .lawFirmMatterId(matter.getLawFirmMatterId())
                .clientId(matter.getClientId())
                .clientName(matter.getClientName())
                .status(matter.getStatus())
                .validDays(matter.getValidDays())
                .expiresAt(matter.getExpiresAt())
                .accessUrl(matter.getAccessUrl())
                .createdAt(matter.getCreatedAt())
                .updatedAt(matter.getUpdatedAt())
                .build();
    }

    /**
     * 获取项目详情（管理后台使用）
     *
     * @param id 项目ID
     * @return 项目详情DTO
     */
    public MatterDetailDTO getMatterDetail(final String id) {
        ClientMatter matter = getMatterById(id);

        // 解析项目数据JSON（初始化为空Map避免NPE）
        Map<String, Object> matterDataMap = new java.util.HashMap<>();
        if (matter.getMatterData() != null && !matter.getMatterData().isEmpty()) {
            try {
                matterDataMap = objectMapper.readValue(matter.getMatterData(), 
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析项目数据JSON失败: id={}", id, e);
            }
        }

        return MatterDetailDTO.builder()
                .id(matter.getId())
                .lawFirmMatterId(matter.getLawFirmMatterId())
                .clientId(matter.getClientId())
                .clientName(matter.getClientName())
                .matterData(matterDataMap)
                .scopes(matter.getScopes())
                .status(matter.getStatus())
                .validDays(matter.getValidDays())
                .expiresAt(matter.getExpiresAt())
                .accessToken(matter.getAccessToken())
                .accessUrl(matter.getAccessUrl())
                .createdAt(matter.getCreatedAt())
                .updatedAt(matter.getUpdatedAt())
                .build();
    }
}
