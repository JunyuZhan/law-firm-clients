package com.clientservice.integration;

import com.clientservice.application.service.CallbackService;
import com.clientservice.application.service.SysConfigService;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.AccessLogMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.clientservice.infrastructure.persistence.mapper.DownloadLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 回调服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("回调服务集成测试")
@SuppressWarnings({"unchecked", "rawtypes"})
class CallbackIntegrationTest {

    @Autowired
    private CallbackService callbackService;

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private DownloadLogMapper downloadLogMapper;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private SysConfigService sysConfigService;

    private String matterId;
    private Long lawFirmMatterId;

    @BeforeEach
    void setUp() {
        // 生成唯一的律所项目ID
        lawFirmMatterId = System.currentTimeMillis();

        // 创建测试项目
        ClientMatter matter = new ClientMatter();
        matter.setId("CS" + System.currentTimeMillis());
        matter.setLawFirmMatterId(lawFirmMatterId);
        matter.setClientId(2001L);
        matter.setClientName("回调测试客户");
        matter.setAccessToken("test-token");
        matter.setStatus("ACTIVE");
        matter.setExpiresAt(LocalDateTime.now().plusDays(30));
        clientMatterMapper.insert(matter);
        matterId = matter.getId();
        lawFirmMatterId = matter.getLawFirmMatterId();

        // 设置回调地址
        ReflectionTestUtils.setField(callbackService, "defaultLawFirmCallbackUrl", "http://example.com");
        ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", true);

        // Mock SysConfigService 返回回调地址和启用回调
        when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), eq("http://example.com")))
                .thenReturn("http://example.com");
        when(sysConfigService.getBooleanConfig(eq("callback.enabled"), eq(true)))
                .thenReturn(true);
    }

    @Test
    @DisplayName("访问日志回调应该成功")
    void callbackAccessLog_ShouldSuccess() throws InterruptedException {
        // Given - 确保项目存在
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        assertNotNull(matter, "测试项目应该存在");
        
        AccessLog accessLog = AccessLog.builder()
                .matterId(matterId)
                .clientId(2001L)
                .accessToken("test-token")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .accessTime(LocalDateTime.now())
                .build();
        accessLogMapper.insert(accessLog);
        
        // 刷新事务，确保数据对异步线程可见
        accessLogMapper.selectById(accessLog.getId()); // 触发事务刷新

        // 重置 mock，避免与其他测试冲突
        reset(restTemplate);
        // 使用 atLeastOnce() 而不是 times(1)，因为可能有多次调用（setUp 中的插入操作也可能触发回调）
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // When
        callbackService.callbackAccessLog(accessLog);

        // Wait for async execution (increase wait time for async task)
        Thread.sleep(2000);

        // Then - 使用 ArgumentCaptor 捕获所有调用，并查找匹配的项
        ArgumentCaptor<org.springframework.http.HttpEntity> entityCaptor = ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        verify(restTemplate, atLeastOnce()).exchange(
                 eq("http://example.com/api/open/client/access-log"),
                any(),
                entityCaptor.capture(),
                eq(String.class));

        boolean found = false;
        for (org.springframework.http.HttpEntity entity : entityCaptor.getAllValues()) {
            Map<String, Object> data = (Map<String, Object>) entity.getBody();
            if (data != null && lawFirmMatterId.equals(data.get("matterId"))) {
                assertEquals(2001L, data.get("clientId"));
                assertEquals("ACCESS", data.get("eventType"));
                found = true;
                break;
            }
        }
        assertTrue(found, "Should have called callback with correct matterId: " + lawFirmMatterId);
    }

    @Test
    @DisplayName("下载日志回调应该成功")
    void callbackDownloadLog_ShouldSuccess() throws InterruptedException {
        // Given - 确保项目存在
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        assertNotNull(matter, "测试项目应该存在");
        
        DownloadLog downloadLog = DownloadLog.builder()
                .matterId(matterId)
                .clientId(2001L)
                .fileId("CS1234567890123456789")
                .fileName("判决书.pdf")
                .accessToken("test-token")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .downloadTime(LocalDateTime.now())
                .build();
        downloadLogMapper.insert(downloadLog);
        
        // 刷新事务，确保数据对异步线程可见
        downloadLogMapper.selectById(downloadLog.getId()); // 触发事务刷新

        reset(restTemplate); // 重置 mock，避免与其他测试冲突
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // When
        callbackService.callbackDownloadLog(downloadLog);

        // Wait for async execution (increase wait time for async task)
        Thread.sleep(2000);

        // Then
        ArgumentCaptor<org.springframework.http.HttpEntity> entityCaptor = ArgumentCaptor.forClass(org.springframework.http.HttpEntity.class);
        verify(restTemplate, atLeastOnce()).exchange(
                 eq("http://example.com/api/open/client/download-log"),
                any(),
                entityCaptor.capture(),
                eq(String.class));

        boolean found = false;
        for (org.springframework.http.HttpEntity entity : entityCaptor.getAllValues()) {
            Map<String, Object> data = (Map<String, Object>) entity.getBody();
            if (data != null && lawFirmMatterId.equals(data.get("matterId"))) {
                assertEquals(2001L, data.get("clientId"));
                assertEquals("CS1234567890123456789", data.get("fileId"));
                assertEquals("判决书.pdf", data.get("fileName"));
                assertEquals("DOWNLOAD", data.get("eventType"));
                found = true;
                break;
            }
        }
        assertTrue(found, "Should have called callback with correct matterId: " + lawFirmMatterId);
    }

    @Test
    @DisplayName("回调失败不应该抛出异常")
    void callback_ShouldNotThrowException_WhenCallbackFails() throws InterruptedException {
        // Given
        AccessLog accessLog = AccessLog.builder()
                .matterId(matterId)
                .clientId(2001L)
                .accessToken("test-token")
                .accessTime(LocalDateTime.now())
                .build();
        accessLogMapper.insert(accessLog);
        
        // 刷新事务，确保数据对异步线程可见
        accessLogMapper.selectById(accessLog.getId());

        reset(restTemplate); // 重置 mock，避免与其他测试冲突
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("网络错误"));

        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            callbackService.callbackAccessLog(accessLog);
        });
        
        // Wait for async execution
        Thread.sleep(2000);
    }

    @Test
    @DisplayName("回调未启用时应该跳过")
    void callback_ShouldSkip_WhenDisabled() {
        // Given
        ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", false);
        AccessLog accessLog = AccessLog.builder()
                .matterId(matterId)
                .clientId(2001L)
                .build();
        accessLogMapper.insert(accessLog);

        // When
        callbackService.callbackAccessLog(accessLog);

        // Then
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }
}
