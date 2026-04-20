package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.service.FileService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FileController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileController 单元测试")
class FileControllerTest {

    @Mock
    private FileService fileService;

    @Mock
    private MatterService matterService;

    @Mock
    private com.clientservice.application.service.DownloadLogService downloadLogService;

    @Mock
    private com.clientservice.application.service.CallbackService callbackService;

    @InjectMocks
    private FileController fileController;

    private MockMvc mockMvc;
    private ClientMatter mockMatter;
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // 创建模拟项目
        mockMatter = createClientMatter("CS1234567890123456789");

        // 创建模拟文件
        mockMultipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test file content".getBytes()
        );
    }

    @Nested
    @DisplayName("上传文件测试")
    class UploadFileTests {

        @Test
        @DisplayName("上传文件应该成功")
        void uploadFile_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long clientId = 2001L;
            String token = "test-token";
            ClientFileDTO fileDTO = createClientFileDTO();

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.uploadFile(any())).thenReturn(fileDTO);

            // When & Then
            mockMvc.perform(multipart("/api/client/files/upload")
                            .file(mockMultipartFile)
                            .param("matterId", matterId)
                            .param("clientId", String.valueOf(clientId))
                            .param("token", token)
                            .param("fileCategory", "CONTRACT")
                            .param("description", "测试文件"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.fileName").value("test.pdf"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).uploadFile(any());
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void uploadFile_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            Long clientId = 2001L;
            String token = "test-token";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(multipart("/api/client/files/upload")
                            .file(mockMultipartFile)
                            .param("matterId", matterId)
                            .param("clientId", String.valueOf(clientId))
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("项目ID不匹配"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, never()).uploadFile(any());
        }

        @Test
        @DisplayName("无效的token应该返回错误")
        void uploadFile_WithInvalidToken_ShouldReturnError() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long clientId = 2001L;
            String invalidToken = "invalid-token";

            when(matterService.getMatterByToken(invalidToken)).thenThrow(
                    new BusinessException("404", "项目不存在或token无效"));

            // When & Then
            mockMvc.perform(multipart("/api/client/files/upload")
                            .file(mockMultipartFile)
                            .param("matterId", matterId)
                            .param("clientId", String.valueOf(clientId))
                            .param("token", invalidToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("404"));

            verify(matterService, times(1)).getMatterByToken(invalidToken);
            verify(fileService, never()).uploadFile(any());
        }
    }

    @Nested
    @DisplayName("获取文件列表测试")
    class GetFilesTests {

        @Test
        @DisplayName("获取文件列表应该成功")
        void getFiles_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            List<ClientFileDTO> files = Arrays.asList(
                    createClientFileDTO(),
                    createClientFileDTO()
            );

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getFilesByMatterId(matterId, null)).thenReturn(files);

            // When & Then
            mockMvc.perform(get("/api/client/files")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getFilesByMatterId(matterId, null);
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void getFiles_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            String token = "test-token";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(get("/api/client/files")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, never()).getFilesByMatterId(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("获取文件详情测试")
    class GetFileTests {

        @Test
        @DisplayName("获取文件详情应该成功")
        void getFile_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFileDTO fileDTO = createClientFileDTO();

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileById(fileId)).thenReturn(fileDTO);

            // When & Then
            mockMvc.perform(get("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(fileId))
                    .andExpect(jsonPath("$.data.storagePath").doesNotExist())
                    .andExpect(jsonPath("$.data.fileHash").doesNotExist());

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileById(fileId);
        }

        @Test
        @DisplayName("文件不存在应该返回404")
        void getFile_WithNonExistentFile_ShouldReturn404() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "non-existent";

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileById(fileId)).thenThrow(
                    new BusinessException("404", "文件不存在"));

            // When & Then
            mockMvc.perform(get("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("404"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileById(fileId);
        }

        @Test
        @DisplayName("文件详情在项目不匹配时应该返回403")
        void getFile_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";

            ClientMatter wrongMatter = createClientMatter("CS9999999999999999999");
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            mockMvc.perform(get("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("项目ID不匹配"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, never()).getActiveFileById(anyString());
        }

        @Test
        @DisplayName("文件不属于该项目时应该返回403")
        void getFile_WithFileNotBelongingToMatter_ShouldReturn403() throws Exception {
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFileDTO fileDTO = createClientFileDTO();
            fileDTO.setMatterId("different-matter-id");

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileById(fileId)).thenReturn(fileDTO);

            mockMvc.perform(get("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("文件不属于该项目"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileById(fileId);
        }
    }

    @Nested
    @DisplayName("下载文件测试")
    class DownloadFileTests {

        @Test
        @DisplayName("下载文件应该成功")
        void downloadFile_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFile fileEntity = createClientFileEntity(fileId);
            Resource mockResource = mock(Resource.class);

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileEntity(fileId)).thenReturn(fileEntity);
            when(fileService.getFileResource(fileId)).thenReturn(mockResource);

            // When & Then
            mockMvc.perform(get("/api/client/files/{fileId}/download", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileEntity(fileId);
            verify(fileService, times(1)).getFileResource(fileId);
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void downloadFile_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            String token = "test-token";
            String fileId = "file-id-123";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(get("/api/client/files/{fileId}/download", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isForbidden());

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, never()).getActiveFileEntity(anyString());
        }

        @Test
        @DisplayName("文件不属于该项目应该返回403")
        void downloadFile_WithFileNotBelongingToMatter_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFile fileEntity = createClientFileEntity(fileId);
            fileEntity.setMatterId("different-matter-id"); // 不同的项目ID

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileEntity(fileId)).thenReturn(fileEntity);

            // When & Then
            mockMvc.perform(get("/api/client/files/{fileId}/download", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isForbidden());

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileEntity(fileId);
            verify(fileService, never()).getFileResource(anyString());
        }
    }

    @Nested
    @DisplayName("删除文件测试")
    class DeleteFileTests {

        @Test
        @DisplayName("删除文件应该成功")
        void deleteFile_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFileDTO fileDTO = createClientFileDTO();
            fileDTO.setMatterId(matterId);

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileById(fileId)).thenReturn(fileDTO);
            doNothing().when(fileService).deleteFile(fileId);

            // When & Then
            mockMvc.perform(delete("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileById(fileId);
            verify(fileService, times(1)).deleteFile(fileId);
        }

        @Test
        @DisplayName("文件不属于该项目应该返回403")
        void deleteFile_WithFileNotBelongingToMatter_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            String fileId = "file-id-123";
            ClientFileDTO fileDTO = createClientFileDTO();
            fileDTO.setMatterId("different-matter-id"); // 不同的项目ID

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(fileService.getActiveFileById(fileId)).thenReturn(fileDTO);

            // When & Then
            mockMvc.perform(delete("/api/client/files/{fileId}", fileId)
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("文件不属于该项目"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(fileService, times(1)).getActiveFileById(fileId);
            verify(fileService, never()).deleteFile(anyString());
        }
    }

    /**
     * 创建测试用的ClientMatter
     */
    private ClientMatter createClientMatter(String matterId) {
        ClientMatter matter = new ClientMatter();
        matter.setId(matterId);
        matter.setClientId(2001L);
        matter.setClientName("测试客户");
        matter.setAccessToken("test-token");
        matter.setAccessUrl("http://localhost:8081/portal/matter/" + matterId + "?token=test-token");
        matter.setValidDays(30);
        matter.setStatus(ClientMatter.STATUS_ACTIVE);
        matter.setCreatedAt(LocalDateTime.now());
        matter.setUpdatedAt(LocalDateTime.now());
        matter.setDeleted(false);
        return matter;
    }

    /**
     * 创建测试用的ClientFileDTO
     */
    private ClientFileDTO createClientFileDTO() {
        return ClientFileDTO.builder()
                .id("file-id-123")
                .matterId("CS1234567890123456789")
                .clientId(2001L)
                .fileName("test.pdf")
                .fileSize(1024L)
                .fileType("application/pdf")
                .fileCategory("CONTRACT")
                .description("测试文件")
                .uploadedAt(LocalDateTime.now())
                .status(ClientFile.STATUS_ACTIVE)
                .build();
    }

    /**
     * 创建测试用的ClientFile实体
     */
    private ClientFile createClientFileEntity(String fileId) {
        ClientFile file = new ClientFile();
        file.setId(fileId);
        file.setMatterId("CS1234567890123456789");
        file.setClientId(2001L);
        file.setFileName("test.pdf");
        file.setFileSize(1024L);
        file.setFileType("application/pdf");
        file.setFileCategory("CONTRACT");
        file.setDescription("测试文件");
        file.setStoragePath("matters/CS1234567890123456789/" + fileId + ".pdf");
        file.setUploadedAt(LocalDateTime.now());
        file.setStatus(ClientFile.STATUS_ACTIVE);
        file.setDeleted(false);
        return file;
    }
}
