package com.clientservice.application.service;

import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.CallbackTask;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ApiKeyMapper;
import com.clientservice.infrastructure.persistence.mapper.CallbackTaskMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackServiceOutboxTest {

    @Mock
    private ClientMatterMapper matterMapper;

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @Mock
    private CallbackTaskMapper callbackTaskMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SysConfigService sysConfigService;

    @Mock
    private UrlGenerator urlGenerator;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private CallbackService callbackService;

    private ClientMatter testMatter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callbackService, "defaultLawFirmCallbackUrl", "http://example.com");
        ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", true);
        ReflectionTestUtils.setField(callbackService, "defaultOutboxMaxRetries", 3);
        ReflectionTestUtils.setField(callbackService, "outboxRetryDelayMinutes", 5);
        ReflectionTestUtils.setField(callbackService, "outboxBatchSize", 100);
        ReflectionTestUtils.setField(callbackService, "staleSendingMinutes", 10);
        ReflectionTestUtils.setField(callbackService, "objectMapper", new ObjectMapper());

        testMatter = new ClientMatter();
        testMatter.setId("CS1706860800000123456");
        testMatter.setLawFirmMatterId(456L);
        testMatter.setClientId(2001L);
    }

    @Test
    @DisplayName("访问日志回调应先持久化出站任务再投递")
    void callbackAccessLog_ShouldPersistOutboxTask() {
        AccessLog accessLog = AccessLog.builder()
                .matterId("CS1706860800000123456")
                .clientId(2001L)
                .accessTime(LocalDateTime.now())
                .build();
        AtomicReference<CallbackTask> insertedTask = new AtomicReference<>();

        when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
        when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                .thenReturn("http://example.com");
        when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(callbackTaskMapper.insert(any(CallbackTask.class))).thenAnswer(invocation -> {
            CallbackTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            insertedTask.set(task);
            return 1;
        });
        when(callbackTaskMapper.selectById(1L)).thenAnswer(invocation -> insertedTask.get());
        when(callbackTaskMapper.update(any(CallbackTask.class), any())).thenReturn(1);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        callbackService.callbackAccessLog(accessLog);

        assertNotNull(insertedTask.get());
        assertEquals(CallbackTask.TYPE_ACCESS_LOG, insertedTask.get().getCallbackType());
        assertEquals("http://example.com/api/open/client/access-log", insertedTask.get().getCallbackUrl());
        verify(callbackTaskMapper).insert(any(CallbackTask.class));
        verify(restTemplate).exchange(
                eq("http://example.com/api/open/client/access-log"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class));
    }

    @Test
    @DisplayName("补偿重试失败时应回写待重试状态")
    void retryPendingCallbacks_ShouldRescheduleTaskWhenDeliveryFails() {
        CallbackTask task = CallbackTask.builder()
                .id(2L)
                .matterId("CS1706860800000123456")
                .callbackType(CallbackTask.TYPE_DOWNLOAD_LOG)
                .callbackUrl("http://example.com/api/open/client/download-log")
                .callbackPayload("{\"matterId\":456,\"clientId\":2001,\"eventType\":\"DOWNLOAD\"}")
                .status(CallbackTask.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
        task.setUpdatedAt(LocalDateTime.now().minusMinutes(20));

        when(callbackTaskMapper.selectList(any())).thenReturn(List.of(task));
        when(callbackTaskMapper.selectById(2L)).thenReturn(task);
        when(callbackTaskMapper.update(any(CallbackTask.class), any())).thenReturn(1);
        when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new RestClientException("network down"));

        callbackService.retryPendingCallbacks();

        ArgumentCaptor<CallbackTask> updateCaptor = ArgumentCaptor.forClass(CallbackTask.class);
        verify(callbackTaskMapper, times(2)).update(updateCaptor.capture(), any());
        assertEquals(CallbackTask.STATUS_SENDING, updateCaptor.getAllValues().get(0).getStatus());
        assertEquals(CallbackTask.STATUS_PENDING, updateCaptor.getAllValues().get(1).getStatus());
        assertEquals(1, updateCaptor.getAllValues().get(1).getRetryCount());
        assertNotNull(updateCaptor.getAllValues().get(1).getNextRetryAt());
    }
}
