package com.clientservice.application.service;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.dto.ClientFileUploadRequest;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.infrastructure.persistence.mapper.ClientFileMapper;
import com.clientservice.infrastructure.scanner.VirusScanResult;
import com.clientservice.infrastructure.scanner.VirusScanner;
import com.clientservice.infrastructure.scanner.VirusScannerFactory;
import com.clientservice.infrastructure.storage.StorageStrategy;
import com.clientservice.infrastructure.storage.StorageStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * FileService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileService 单元测试")
class FileServiceTest {

    @Mock
    private ClientFileMapper fileMapper;

    @Mock
    private StorageStrategyFactory storageStrategyFactory;

    @Mock
    private VirusScannerFactory virusScannerFactory;

    @Mock
    private StorageStrategy storageStrategy;

    @Mock
    private VirusScanner virusScanner;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    private MultipartFile mockFile;
    private ClientFileUploadRequest uploadRequest;

    @BeforeEach
    void setUp() throws Exception {
        // 设置maxFileSize字段
        ReflectionTestUtils.setField(fileService, "maxFileSize", 10485760L); // 10MB
        // 设置文件验证配置
        ReflectionTestUtils.setField(fileService, "allowedExtensions", ".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.bmp,.txt,.zip,.rar");
        ReflectionTestUtils.setField(fileService, "validateExtension", true);
        ReflectionTestUtils.setField(fileService, "validateContent", false); // 测试时禁用内容验证，避免需要真实的文件内容

        // Mock存储策略 - 使用lenient避免不必要的stubbing警告
        lenient().when(storageStrategyFactory.getStorageStrategy()).thenReturn(storageStrategy);
        lenient().when(storageStrategy.uploadFile(any(), anyString())).thenReturn("matters/test/file.pdf");
        lenient().when(storageStrategy.downloadFile(anyString())).thenReturn(mock(Resource.class));
        lenient().doNothing().when(storageStrategy).deleteFile(anyString());

        // Mock病毒扫描器 - 使用lenient避免不必要的stubbing警告
        lenient().when(virusScannerFactory.getVirusScanner()).thenReturn(virusScanner);
        lenient().when(virusScanner.scanFile(any())).thenReturn(VirusScanResult.safe("Test"));

        // 创建模拟文件 - 使用lenient避免UnnecessaryStubbingException
        mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        lenient().when(mockFile.getContentType()).thenReturn("application/pdf");
        lenient().when(mockFile.getSize()).thenReturn(1024L);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        // 提供一个简单的输入流用于哈希计算
        lenient().when(mockFile.getInputStream()).thenAnswer(invocation -> new java.io.ByteArrayInputStream("test content".getBytes()));

        uploadRequest = new ClientFileUploadRequest();
        uploadRequest.setMatterId("CS1234567890123456789");
        uploadRequest.setClientId(2001L);
        uploadRequest.setFile(mockFile);
        uploadRequest.setFileCategory("CONTRACT");
        uploadRequest.setDescription("测试文件");
    }

    @Nested
    @DisplayName("上传文件测试")
    class UploadFileTests {

        @Test
        @DisplayName("检测到重复文件时应该返回已有记录")
        void uploadFile_DuplicateFile_ShouldReturnExisting() throws Exception {
            // Given
            ClientFile existingFile = new ClientFile();
            existingFile.setId("existing-file-id");
            existingFile.setFileName("test.pdf");
            existingFile.setMatterId("CS1234567890123456789");
            
            // Mock file hash check
            when(fileMapper.selectByMatterIdAndFileHash(anyString(), anyString())).thenReturn(existingFile);

            // When
            ClientFileDTO result = fileService.uploadFile(uploadRequest);

            // Then
            assertNotNull(result);
            assertEquals("existing-file-id", result.getId());
            // 验证没有调用 insert 和 storageStrategy.uploadFile
            verify(fileMapper, never()).insert(any(ClientFile.class));
            verify(storageStrategy, never()).uploadFile(any(), anyString());
        }

        @Test
    @DisplayName("上传文件应该成功")
    void uploadFile_ShouldSuccess() throws Exception {
        // Given
        when(storageStrategy.uploadFile(any(), anyString())).thenReturn("matters/test/file.pdf");
        when(fileMapper.insert(any(ClientFile.class))).thenAnswer(invocation -> {
            // 模拟插入后返回
            return null;
        });
        // 模拟没有重复文件
        when(fileMapper.selectByMatterIdAndFileHash(anyString(), anyString())).thenReturn(null);

        // When
        ClientFileDTO result = fileService.uploadFile(uploadRequest);

        // Then
        assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals("test.pdf", result.getFileName());
            assertEquals("CONTRACT", result.getFileCategory());
            assertEquals("测试文件", result.getDescription());
            verify(fileMapper, times(1)).insert(any(ClientFile.class));
            verify(storageStrategy, times(1)).uploadFile(any(), anyString());
        }

        @Test
        @DisplayName("文件为空应该抛出异常")
        void uploadFile_WithEmptyFile_ShouldThrowException() {
            // Given
            when(mockFile.isEmpty()).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("文件不能为空"));
        }

        @Test
        @DisplayName("文件大小超过限制应该抛出异常")
        void uploadFile_WithOversizedFile_ShouldThrowException() {
            // Given
            when(mockFile.getSize()).thenReturn(10485761L); // 超过10MB

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("文件大小超过限制"));
        }

        @Test
        @DisplayName("文件名称为空应该抛出异常")
        void uploadFile_WithEmptyFilename_ShouldThrowException() {
            // Given
            when(mockFile.getOriginalFilename()).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("文件名不能为空"));
        }

        @Test
        @DisplayName("不允许的文件扩展名应该抛出异常")
        void uploadFile_WithInvalidExtension_ShouldThrowException() {
            // Given
            when(mockFile.getOriginalFilename()).thenReturn("test.exe"); // .exe 不在白名单中

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("不允许的文件类型"));
        }

        @Test
        @DisplayName("文件没有扩展名应该抛出异常")
        void uploadFile_WithNoExtension_ShouldThrowException() {
            // Given
            when(mockFile.getOriginalFilename()).thenReturn("testfile"); // 没有扩展名

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("文件必须包含扩展名"));
        }

        @Test
        @DisplayName("文件传输失败应该抛出异常")
        void uploadFile_WithTransferFailure_ShouldThrowException() throws Exception {
            // Given
            when(storageStrategy.uploadFile(any(), anyString())).thenThrow(new Exception("传输失败"));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.uploadFile(uploadRequest));
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getCode());
            assertTrue(exception.getMessage().contains("文件上传失败") || exception.getMessage().contains("保存文件失败"));
        }
    }

    @Nested
    @DisplayName("获取文件测试")
    class GetFileTests {

        @Test
        @DisplayName("根据ID获取文件应该成功")
        void getFileById_ShouldSuccess() {
            // Given
            ClientFile file = createTestFile();
            when(fileMapper.selectById("file-id-123")).thenReturn(file);

            // When
            ClientFileDTO result = fileService.getFileById("file-id-123");

            // Then
            assertNotNull(result);
            assertEquals("file-id-123", result.getId());
            assertEquals("test.pdf", result.getFileName());
        }

        @Test
        @DisplayName("文件不存在应该抛出异常")
        void getFileById_WithNonExistentFile_ShouldThrowException() {
            // Given
            when(fileMapper.selectById("non-existent")).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.getFileById("non-existent"));
            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("文件不存在"));
        }

        @Test
        @DisplayName("根据项目ID获取文件列表应该成功")
        void getFilesByMatterId_ShouldSuccess() {
            // Given
            ClientFile file1 = createTestFile();
            file1.setId("file-1");
            ClientFile file2 = createTestFile();
            file2.setId("file-2");
            when(fileMapper.selectByMatterId("matter-id", null))
                    .thenReturn(Arrays.asList(file1, file2));

            // When
            List<ClientFileDTO> result = fileService.getFilesByMatterId("matter-id", null);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("下载文件应该成功")
        void downloadFile_ShouldSuccess() throws Exception {
            // Given
            ClientFile file = createTestFile();
            when(fileMapper.selectById("file-id")).thenReturn(file);
            Resource mockResource = mock(Resource.class);
            when(storageStrategy.downloadFile(anyString())).thenReturn(mockResource);

            // When
            Resource result = fileService.getFileResource("file-id");

            // Then
            assertNotNull(result);
            assertEquals(mockResource, result);
            verify(storageStrategy, times(1)).downloadFile("matters/matter-id/file-id-123.pdf");
        }

        @Test
        @DisplayName("下载不存在的文件应该抛出异常")
        void downloadFile_WithNonExistentFile_ShouldThrowException() {
            // Given
            when(fileMapper.selectById("non-existent")).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.getFileResource("non-existent"));
            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
        }

        @Test
        @DisplayName("下载不可用的文件应该抛出异常")
        void downloadFile_WithInactiveFile_ShouldThrowException() {
            // Given
            ClientFile file = createTestFile();
            file.setStatus(ClientFile.STATUS_DELETED);
            when(fileMapper.selectById("inactive-file")).thenReturn(file);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.getFileResource("inactive-file"));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
        }
    }

    @Nested
    @DisplayName("文件清理和统计测试")
    class CleanupAndStatisticsTests {

        @Test
        @DisplayName("清理已删除文件应该成功")
        void cleanupDeletedFiles_ShouldSuccess() throws Exception {
            // Given
            ClientFile file1 = createTestFile();
            file1.setId("file-1");
            ClientFile file2 = createTestFile();
            file2.setId("file-2");
            
            when(fileMapper.selectDeletedBefore(any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(file1, file2))
                    .thenReturn(java.util.Collections.emptyList()); // 第二次调用返回空列表，结束循环
            doNothing().when(storageStrategy).deleteFile(anyString());
            when(fileMapper.deleteById(anyString())).thenReturn(1);

            // When
            int result = fileService.cleanupDeletedFiles(30);

            // Then
            assertEquals(2, result);
            verify(storageStrategy, times(2)).deleteFile(anyString());
            verify(fileMapper, times(2)).deleteById(anyString());
            // 验证 selectDeletedBefore 被调用了两次（一次返回数据，一次返回空）
            verify(fileMapper, times(2)).selectDeletedBefore(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("获取文件统计信息应该成功")
        void getFileStatistics_ShouldSuccess() {
            // Given
            when(fileMapper.countWithFilter(any(), any(), any(), any())).thenReturn(10L);
            when(fileMapper.sumFileSize(any())).thenReturn(10240L);
            when(fileMapper.countByCategory(anyString())).thenReturn(2L);
            when(fileMapper.countDistinctMatters()).thenReturn(5L);

            // When
            java.util.Map<String, Object> stats = fileService.getFileStatistics();

            // Then
            assertNotNull(stats);
            assertEquals(10L, stats.get("totalCount"));
            assertEquals(10240L, stats.get("totalSize"));
            assertEquals(5L, stats.get("matterCount"));
        }
    }

    @Nested
    @DisplayName("删除文件测试")
    class DeleteFileTests {

        @Test
        @DisplayName("删除文件应该成功")
        void deleteFile_ShouldSuccess() throws Exception {
            // Given
            ClientFile file = createTestFile();
            file.setStoragePath("matters/matter-id/file-id.pdf");
            when(fileMapper.selectById("file-id")).thenReturn(file);
            when(fileMapper.updateById(any(ClientFile.class))).thenReturn(1);
            doNothing().when(storageStrategy).deleteFile(anyString());

            // When
            assertDoesNotThrow(() -> fileService.deleteFile("file-id"));

            // Then
            verify(fileMapper, times(1)).updateById(any(ClientFile.class));
            verify(storageStrategy, times(1)).deleteFile("matters/matter-id/file-id.pdf");
        }

        @Test
        @DisplayName("删除不存在的文件应该抛出异常")
        void deleteFile_WithNonExistentFile_ShouldThrowException() {
            // Given
            when(fileMapper.selectById("non-existent")).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> fileService.deleteFile("non-existent"));
            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("文件不存在"));
        }
    }

    /**
     * 创建测试用的ClientFile对象
     */
    private ClientFile createTestFile() {
        ClientFile file = new ClientFile();
        file.setId("file-id-123");
        file.setMatterId("matter-id");
        file.setClientId(2001L);
        file.setFileName("test.pdf");
        file.setFileSize(1024L);
        file.setFileType("application/pdf");
        file.setFileCategory("CONTRACT");
        file.setDescription("测试文件");
        file.setStoragePath("matters/matter-id/file-id-123.pdf");
        file.setUploadedAt(LocalDateTime.now());
        file.setStatus(ClientFile.STATUS_ACTIVE);
        file.setDeleted(false);
        return file;
    }
}
