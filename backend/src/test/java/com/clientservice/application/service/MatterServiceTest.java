package com.clientservice.application.service;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * MatterService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatterService 单元测试")
class MatterServiceTest {

    @Mock
    private ClientMatterMapper clientMatterMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UrlGenerator urlGenerator;

    @InjectMocks
    private MatterService matterService;

    private MatterReceiveRequest request;
    private ApiKey sourceApiKey;

    @BeforeEach
    void setUp() throws Exception {
        // 设置私有字段（@Value注入的字段）
        ReflectionTestUtils.setField(matterService, "defaultValidDays", 30);
        ReflectionTestUtils.setField(matterService, "tokenLength", 32);
        
        // 设置UrlGenerator的baseUrl - 使用lenient避免UnnecessaryStubbingException
        // 支持两个参数和三个参数的版本
        lenient().when(urlGenerator.generateAccessUrl(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String matterId = invocation.getArgument(0);
                    String token = invocation.getArgument(1);
                    return "http://localhost:8081/portal/matter/" + matterId + "?token=" + token;
                });
        lenient().when(urlGenerator.generateAccessUrl(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String matterId = invocation.getArgument(0);
                    String token = invocation.getArgument(1);
                    return "http://localhost:8081/portal/matter/" + matterId + "?token=" + token;
                });
        
        // 设置ObjectMapper行为 - 使用lenient避免UnnecessaryStubbingException
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"matterId\":1001,\"matterName\":\"测试项目\"}");
        
        request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 1001);
        matterData.put("matterName", "测试项目");
        request.setMatterData(matterData);
        
        request.setScopes(Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
        request.setValidDays(30);

        sourceApiKey = new ApiKey();
        sourceApiKey.setId(1L);
    }

    @Nested
    @DisplayName("接收项目数据测试")
    class ReceiveMatterDataTests {

        @Test
        @DisplayName("正常接收项目数据")
        void receiveMatterData_ShouldSuccess() {
            // Given
            when(clientMatterMapper.selectBySourceAndLawFirmMatterId(anyLong(), anyLong()))
                    .thenReturn(null);
            when(clientMatterMapper.insert(any(ClientMatter.class))).thenReturn(1);
            doNothing().when(notificationService).sendNotificationAsync(any(ClientMatter.class));

            // When
            var result = matterService.receiveMatterData(request, null, sourceApiKey);

            // Then
            assertNotNull(result);
            assertNotNull(result.getId());
            assertNotNull(result.getAccessUrl());
            verify(clientMatterMapper, times(1)).insert(any(ClientMatter.class));
            verify(notificationService, times(1)).sendNotificationAsync(any(ClientMatter.class));
        }

        @Test
        @DisplayName("更新已存在的项目")
        void receiveMatterData_WithExistingMatter_ShouldUpdate() {
            // Given
            ClientMatter existing = new ClientMatter();
            existing.setId("CS1706860800000123456");
            existing.setStatus("ACTIVE");
            
            when(clientMatterMapper.selectBySourceAndLawFirmMatterId(anyLong(), anyLong()))
                    .thenReturn(existing);
            when(clientMatterMapper.updateById(any(ClientMatter.class))).thenReturn(1);
            doNothing().when(notificationService).sendNotificationAsync(any(ClientMatter.class));

            // When
            var result = matterService.receiveMatterData(request, null, sourceApiKey);

            // Then
            assertNotNull(result);
            assertEquals(existing.getId(), result.getId());
            verify(clientMatterMapper, times(1)).updateById(any(ClientMatter.class));
            verify(clientMatterMapper, never()).insert(any(ClientMatter.class));
        }
    }

    @Nested
    @DisplayName("根据Token获取项目测试")
    class GetMatterByTokenTests {

        @Test
        @DisplayName("有效Token应该返回项目")
        void getMatterByToken_WithValidToken_ShouldReturnMatter() {
            // Given
            String token = "test-token-12345678901234567890123456789012";
            ClientMatter matter = new ClientMatter();
            matter.setId("CS1706860800000123456");
            matter.setAccessToken(token);
            matter.setStatus("ACTIVE");
            matter.setExpiresAt(LocalDateTime.now().plusDays(30));
            
            when(clientMatterMapper.selectByAccessToken(token)).thenReturn(matter);

            // When
            ClientMatter result = matterService.getMatterByToken(token);

            // Then
            assertNotNull(result);
            assertEquals(token, result.getAccessToken());
        }

        @Test
        @DisplayName("无效Token应该抛出异常")
        void getMatterByToken_WithInvalidToken_ShouldThrowException() {
            // Given
            String token = "invalid-token";
            when(clientMatterMapper.selectByAccessToken(token)).thenReturn(null);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                matterService.getMatterByToken(token);
            });
        }

        @Test
        @DisplayName("过期项目应该抛出异常")
        void getMatterByToken_WithExpiredMatter_ShouldThrowException() {
            // Given
            String token = "test-token";
            ClientMatter matter = new ClientMatter();
            matter.setId("CS1706860800000123456");
            matter.setAccessToken(token);
            matter.setStatus("ACTIVE");
            matter.setExpiresAt(LocalDateTime.now().minusDays(1));
            
            when(clientMatterMapper.selectByAccessToken(token)).thenReturn(matter);
            when(clientMatterMapper.updateStatus(anyString(), anyString())).thenReturn(1);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                matterService.getMatterByToken(token);
            });
            verify(clientMatterMapper, times(1)).updateStatus(anyString(), eq("EXPIRED"));
        }
    }

    @Nested
    @DisplayName("根据ID获取项目测试")
    class GetMatterByIdTests {

        @Test
        @DisplayName("来源匹配时应该返回项目")
        void getMatterByIdForSource_WithMatchingSource_ShouldReturnMatter() throws Exception {
            String matterId = "CS1706860800000123456";
            ClientMatter matter = new ClientMatter();
            matter.setId(matterId);
            matter.setDeleted(false);
            matter.setSourceApiKeyId(1L);

            when(clientMatterMapper.selectById(matterId)).thenReturn(matter);

            ClientMatter result = matterService.getMatterByIdForSource(matterId, 1L);

            assertNotNull(result);
            assertEquals(matterId, result.getId());
        }

        @Test
        @DisplayName("来源不匹配时应该抛出403")
        void getMatterByIdForSource_WithMismatchedSource_ShouldThrowForbidden() throws Exception {
            String matterId = "CS1706860800000123456";
            ClientMatter matter = new ClientMatter();
            matter.setId(matterId);
            matter.setDeleted(false);
            matter.setSourceApiKeyId(2L);

            when(clientMatterMapper.selectById(matterId)).thenReturn(matter);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> matterService.getMatterByIdForSource(matterId, 1L));

            assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
            assertTrue(exception.getMessage().contains("无权访问该项目"));
        }
    }

    @Nested
    @DisplayName("撤销项目访问测试")
    class RevokeMatterTests {

        @Test
        @DisplayName("撤销项目访问应该更新状态")
        void revokeMatter_ShouldUpdateStatus() {
            // Given
            String matterId = "CS1706860800000123456";
            ClientMatter matter = new ClientMatter();
            matter.setId(matterId);
            matter.setStatus("ACTIVE");
            matter.setDeleted(false);
            
            when(clientMatterMapper.selectById(matterId)).thenReturn(matter);
            when(clientMatterMapper.updateStatus(eq(matterId), eq("REVOKED"))).thenReturn(1);

            // When
            matterService.revokeMatter(matterId);

            // Then
            verify(clientMatterMapper, times(1)).updateStatus(eq(matterId), eq("REVOKED"));
        }

        @Test
        @DisplayName("不存在的项目应该抛出异常")
        void revokeMatter_WithNonExistentMatter_ShouldThrowException() {
            // Given
            String matterId = "non-existent";
            when(clientMatterMapper.selectById(matterId)).thenReturn(null);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                matterService.revokeMatter(matterId);
            });
        }
    }
}
