package com.clientservice.integration;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.dto.ClientFileUploadRequest;
import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.service.FileService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件管理集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("文件管理集成测试")
class FileIntegrationTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private MatterService matterService;

    private String matterId;
    private Long clientId;

    @BeforeEach
    void setUp() {
        // 创建测试项目
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("文件测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 2001L);
        matterData.put("matterName", "文件测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);
        
        var response = matterService.receiveMatterData(request);
        matterId = response.getId();
        clientId = 2001L;
    }

    @Test
    @DisplayName("上传文件应该成功")
    void uploadFile_ShouldSuccess() {
        // Given
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test file content".getBytes()
        );
        
        ClientFileUploadRequest uploadRequest = new ClientFileUploadRequest();
        uploadRequest.setMatterId(matterId);
        uploadRequest.setClientId(clientId);
        uploadRequest.setFile(file);
        uploadRequest.setFileCategory("CONTRACT");
        uploadRequest.setDescription("测试文件");

        // When
        ClientFileDTO result = fileService.uploadFile(uploadRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test.pdf", result.getFileName());
        assertEquals("CONTRACT", result.getFileCategory());
        assertEquals("测试文件", result.getDescription());
    }

    @Test
    @DisplayName("获取文件列表应该返回文件")
    void getFilesByMatterId_ShouldReturnFiles() {
        // Given - 先上传一个文件
        MultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );
        ClientFileUploadRequest uploadRequest = new ClientFileUploadRequest();
        uploadRequest.setMatterId(matterId);
        uploadRequest.setClientId(clientId);
        uploadRequest.setFile(file);
        
        fileService.uploadFile(uploadRequest);

        // When
        List<ClientFileDTO> files = fileService.getFilesByMatterId(matterId, null);

        // Then
        assertNotNull(files);
        assertFalse(files.isEmpty());
    }

    @Test
    @DisplayName("文件大小超过限制应该抛出异常")
    void uploadFile_WithExceededSize_ShouldThrowException() {
        // Given - 创建一个大文件（超过10MB限制）
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile file = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            largeContent
        );
        
        ClientFileUploadRequest uploadRequest = new ClientFileUploadRequest();
        uploadRequest.setMatterId(matterId);
        uploadRequest.setClientId(clientId);
        uploadRequest.setFile(file);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            fileService.uploadFile(uploadRequest);
        });
    }
}
