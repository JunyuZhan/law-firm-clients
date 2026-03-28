package com.clientservice.integration;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 项目数据集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("项目数据集成测试")
class MatterIntegrationTest {

    @Autowired
    private MatterService matterService;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    private MatterReceiveRequest request;

    @BeforeEach
    void setUp() {
        request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("集成测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 1001L);
        matterData.put("matterName", "集成测试项目");
        matterData.put("matterNo", "M20260202001");
        request.setMatterData(matterData);
        
        request.setScopes(Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
        request.setValidDays(30);
    }

    @Test
    @DisplayName("接收项目数据应该成功创建记录")
    void receiveMatterData_ShouldCreateRecord() {
        // When
        MatterReceiveResponse response = matterService.receiveMatterData(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getAccessUrl());
        assertTrue(response.getAccessUrl().contains(response.getId()));

        // 验证数据库记录
        ClientMatter matter = clientMatterMapper.selectById(response.getId());
        assertNotNull(matter);
        assertEquals("ACTIVE", matter.getStatus());
        assertEquals(2001L, matter.getClientId());
        assertEquals("集成测试客户", matter.getClientName());
    }

    @Test
    @DisplayName("使用Token访问项目应该成功")
    void getMatterByToken_ShouldReturnMatter() {
        // Given
        MatterReceiveResponse response = matterService.receiveMatterData(request);
        ClientMatter created = clientMatterMapper.selectById(response.getId());
        String token = created.getAccessToken();

        // When
        ClientMatter result = matterService.getMatterByToken(token);

        // Then
        assertNotNull(result);
        assertEquals(response.getId(), result.getId());
        assertEquals(token, result.getAccessToken());
    }

    @Test
    @DisplayName("无效Token应该抛出异常")
    void getMatterByToken_WithInvalidToken_ShouldThrowException() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            matterService.getMatterByToken("invalid-token");
        });
    }

    @Test
    @DisplayName("更新已存在的项目应该更新记录")
    void receiveMatterData_WithExistingMatter_ShouldUpdate() {
        // Given - 创建初始记录
        MatterReceiveResponse firstResponse = matterService.receiveMatterData(request);
        String matterId = firstResponse.getId();
        
        // 等待一下确保时间戳不同（如果需要）
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - 更新项目数据（使用相同的lawFirmMatterId）
        MatterReceiveRequest updateRequest = new MatterReceiveRequest();
        updateRequest.setClientId(2001L);
        updateRequest.setClientName("更新后的客户名称");
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("matterId", 1001L); // 相同的lawFirmMatterId
        updatedData.put("matterName", "更新后的项目名称");
        updateRequest.setMatterData(updatedData);
        updateRequest.setScopes(Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
        updateRequest.setValidDays(30);
        
        MatterReceiveResponse secondResponse = matterService.receiveMatterData(updateRequest);

        // Then
        assertEquals(matterId, secondResponse.getId());
        ClientMatter updated = clientMatterMapper.selectById(matterId);
        assertNotNull(updated);
        assertEquals("更新后的客户名称", updated.getClientName());
    }
}
