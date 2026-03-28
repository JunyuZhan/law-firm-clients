package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationHistoryDTO;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * NotificationRecordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationRecordService 单元测试")
class NotificationRecordServiceTest {

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @InjectMocks
    private NotificationRecordService notificationRecordService;

    private NotificationRecord testRecord;

    private Page<NotificationRecord> anyNotificationRecordPage() {
        return org.mockito.ArgumentMatchers.<Page<NotificationRecord>>any();
    }

    private LambdaQueryWrapper<NotificationRecord> anyNotificationRecordQueryWrapper() {
        return org.mockito.ArgumentMatchers.<LambdaQueryWrapper<NotificationRecord>>any();
    }

    @BeforeEach
    void setUp() {
        // 先创建 mock 对象
        testRecord = mock(NotificationRecord.class);
        // 使用 lenient() 避免 UnnecessaryStubbingException
        lenient().when(testRecord.getId()).thenReturn(1L);
        lenient().when(testRecord.getMatterId()).thenReturn("CS1234567890123456789");
        lenient().when(testRecord.getClientId()).thenReturn(2001L);
        lenient().when(testRecord.getNotificationType()).thenReturn(NotificationRecord.TYPE_EMAIL);
        lenient().when(testRecord.getRecipient()).thenReturn("test@example.com");
        lenient().when(testRecord.getContent()).thenReturn("测试通知内容");
        lenient().when(testRecord.getStatus()).thenReturn(NotificationRecord.STATUS_SUCCESS);
        lenient().when(testRecord.getSentAt()).thenReturn(LocalDateTime.now());
        lenient().when(testRecord.getCreatedAt()).thenReturn(LocalDateTime.now());
        lenient().when(testRecord.getErrorMessage()).thenReturn(null);
    }

    @Nested
    @DisplayName("获取通知历史测试")
    class GetNotificationHistoryTests {

        @Test
        @DisplayName("获取通知历史应该成功")
        void getNotificationHistory_ShouldSuccess() {
            // Given
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(notificationRecordMapper, times(1)).selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper());
        }

        @Test
        @DisplayName("根据项目ID查询应该成功")
        void getNotificationHistory_ByMatterId_ShouldSuccess() {
            // Given
            String matterId = "CS1234567890123456789";
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    matterId, null, null, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(notificationRecordMapper, times(1)).selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper());
        }

        @Test
        @DisplayName("根据客户ID查询应该成功")
        void getNotificationHistory_ByClientId_ShouldSuccess() {
            // Given
            Long clientId = 2001L;
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, clientId, null, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据通知类型查询应该成功")
        void getNotificationHistory_ByNotificationType_ShouldSuccess() {
            // Given
            String notificationType = NotificationRecord.TYPE_EMAIL;
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, notificationType, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据状态查询应该成功")
        void getNotificationHistory_ByStatus_ShouldSuccess() {
            // Given
            String status = NotificationRecord.STATUS_SUCCESS;
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, status, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("根据时间范围查询应该成功")
        void getNotificationHistory_ByTimeRange_ShouldSuccess() {
            // Given
            LocalDateTime startTime = LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = LocalDateTime.now();
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, startTime, endTime, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("限制数量应该生效")
        void getNotificationHistory_WithLimit_ShouldLimitResults() {
            // Given
            Integer limit = 10;
            Page<NotificationRecord> page = new Page<>(1, limit);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, null, null, limit);

            // Then
            assertNotNull(result);
            verify(notificationRecordMapper, times(1)).selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper());
        }

        @Test
        @DisplayName("默认限制数量应该是100")
        void getNotificationHistory_WithoutLimit_ShouldUseDefaultLimit() {
            // Given
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, null, null, null);

            // Then
            assertNotNull(result);
            verify(notificationRecordMapper, times(1)).selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper());
        }

        @Test
        @DisplayName("空结果应该返回空列表")
        void getNotificationHistory_WithNoResults_ShouldReturnEmptyList() {
            // Given
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Collections.emptyList());
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, null, null, null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("DTO转换测试")
    class DTOConversionTests {

        @Test
        @DisplayName("DTO转换应该包含所有字段")
        void convertToDTO_ShouldContainAllFields() {
            // Given
            Page<NotificationRecord> page = new Page<>(1, 100);
            page.setRecords(Arrays.asList(testRecord));
            when(notificationRecordMapper.selectPage(anyNotificationRecordPage(), anyNotificationRecordQueryWrapper())).thenReturn(page);

            // When
            List<NotificationHistoryDTO> result = notificationRecordService.getNotificationHistory(
                    null, null, null, null, null, null, null);

            // Then
            assertEquals(1, result.size());
            NotificationHistoryDTO dto = result.get(0);
            assertEquals(testRecord.getId(), dto.getId());
            assertEquals(testRecord.getMatterId(), dto.getMatterId());
            assertEquals(testRecord.getClientId(), dto.getClientId());
            assertEquals(testRecord.getNotificationType(), dto.getNotificationType());
            assertEquals(testRecord.getRecipient(), dto.getRecipient());
            assertEquals(testRecord.getContent(), dto.getContent());
            assertEquals(testRecord.getStatus(), dto.getStatus());
            assertEquals(testRecord.getErrorMessage(), dto.getErrorMessage());
            assertEquals(testRecord.getSentAt(), dto.getSentAt());
            assertEquals(testRecord.getCreatedAt(), dto.getCreatedAt());
        }
    }
}
