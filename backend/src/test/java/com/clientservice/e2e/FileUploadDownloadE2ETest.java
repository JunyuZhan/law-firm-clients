package com.clientservice.e2e;

import com.clientservice.application.dto.ClientFileDTO;
import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.service.MatterService;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientFileMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件上传下载端到端测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("文件上传下载端到端测试")
@SuppressWarnings("rawtypes")
class FileUploadDownloadE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatterService matterService;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    @Autowired
    private ClientFileMapper clientFileMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private String matterId;
    private String token;
    private Long clientId;

    @BeforeEach
    void setUp() {
        // 创建测试项目
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(3001L);
        request.setClientName("文件E2E测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 6001L);
        matterData.put("matterName", "文件E2E测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);
        
        MatterReceiveResponse response = matterService.receiveMatterData(request);
        matterId = response.getId();
        clientId = 3001L;
        
        // 获取token
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        token = matter != null ? matter.getAccessToken() : "test-token";
    }

    @Test
    @DisplayName("完整流程：上传文件→查询文件列表→下载文件→删除文件应该成功")
    void fullWorkflow_Upload_List_Download_Delete_ShouldSuccess() throws Exception {
        // ========== Step 1: 上传文件 ==========
        String fileName = "test-document.pdf";
        byte[] fileContent = "This is a test PDF content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", fileName, MediaType.APPLICATION_PDF_VALUE, fileContent);

        String uploadResponse = mockMvc.perform(multipart("/api/client/files/upload")
                        .file(multipartFile)
                        .param("matterId", matterId)
                        .param("clientId", String.valueOf(clientId))
                        .param("token", token)
                        .param("fileCategory", "EVIDENCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.fileName").value(fileName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result result = objectMapper.readValue(uploadResponse, 
                com.clientservice.common.result.Result.class);
        ClientFileDTO uploadedFile = objectMapper.convertValue(result.getData(), ClientFileDTO.class);
        String fileId = uploadedFile.getId();
        assertNotNull(fileId);

        // 验证数据库记录
        ClientFile file = clientFileMapper.selectById(fileId);
        assertNotNull(file);
        assertEquals(fileName, file.getFileName());
        assertEquals("EVIDENCE", file.getFileCategory());
        assertEquals("ACTIVE", file.getStatus());

        // ========== Step 2: 查询文件列表 ==========
        String listResponse = mockMvc.perform(get("/api/client/files")
                        .param("matterId", matterId)
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result listResult = objectMapper.readValue(listResponse, 
                com.clientservice.common.result.Result.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) listResult.getData();
        assertNotNull(files);
        assertFalse(files.isEmpty());
        assertTrue(files.stream().anyMatch(f -> fileId.equals(f.get("id"))));

        // ========== Step 3: 下载文件 ==========
        mockMvc.perform(get("/api/client/files/{id}/download", fileId)
                        .param("matterId", matterId)
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(fileContent));

        // ========== Step 4: 删除文件 ==========
        mockMvc.perform(delete("/api/client/files/{id}", fileId)
                        .param("matterId", matterId)
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证文件已删除（逻辑删除）
        ClientFile deletedFile = clientFileMapper.selectById(fileId);
        assertNotNull(deletedFile);
        assertEquals("DELETED", deletedFile.getStatus());
    }

    @Test
    @DisplayName("上传多个文件应该都成功")
    void uploadMultipleFiles_ShouldAllSuccess() throws Exception {
        int fileCount = 3;
        String[] fileIds = new String[fileCount];

        // 上传多个文件
        for (int i = 0; i < fileCount; i++) {
            String fileName = "test-file-" + i + ".pdf";
            byte[] fileContent = ("Content " + i).getBytes();
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", fileName, MediaType.APPLICATION_PDF_VALUE, fileContent);

            String uploadResponse = mockMvc.perform(multipart("/api/client/files/upload")
                            .file(multipartFile)
                            .param("matterId", matterId)
                            .param("clientId", String.valueOf(clientId))
                            .param("token", token)
                            .param("fileCategory", "EVIDENCE"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            com.clientservice.common.result.Result result = objectMapper.readValue(uploadResponse, 
                    com.clientservice.common.result.Result.class);
            
            // 检查上传是否成功
            assertTrue(result.isSuccess(), "文件上传应该成功: " + result.getMessage());
            assertNotNull(result.getData(), "上传响应应该包含文件数据");
            
            ClientFileDTO uploadedFile = objectMapper.convertValue(result.getData(), ClientFileDTO.class);
            assertNotNull(uploadedFile, "上传的文件DTO不应该为null");
            assertNotNull(uploadedFile.getId(), "上传的文件ID不应该为null");
            fileIds[i] = uploadedFile.getId();
        }

        // 验证所有文件都已上传
        List<ClientFile> files = clientFileMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClientFile>()
                        .eq(ClientFile::getMatterId, matterId)
                        .eq(ClientFile::getStatus, "ACTIVE"));
        assertTrue(files.size() >= fileCount, 
                String.format("应该至少有%d个文件，实际：%d", fileCount, files.size()));
    }

    @Test
    @DisplayName("上传超大文件应该失败")
    void uploadOversizedFile_ShouldFail() throws Exception {
        // 创建超大文件（超过10MB限制）
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "large-file.pdf", MediaType.APPLICATION_PDF_VALUE, largeContent);

        mockMvc.perform(multipart("/api/client/files/upload")
                        .file(multipartFile)
                        .param("matterId", matterId)
                        .param("clientId", String.valueOf(clientId))
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("使用无效Token下载文件应该失败")
    void downloadFileWithInvalidToken_ShouldFail() throws Exception {
        // 先上传一个文件
        String fileName = "test-file.pdf";
        byte[] fileContent = "Test content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", fileName, MediaType.APPLICATION_PDF_VALUE, fileContent);

        String uploadResponse = mockMvc.perform(multipart("/api/client/files/upload")
                        .file(multipartFile)
                        .param("matterId", matterId)
                        .param("clientId", String.valueOf(clientId))
                        .param("token", token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result result = objectMapper.readValue(uploadResponse, 
                com.clientservice.common.result.Result.class);
        
        // 检查上传是否成功
        assertTrue(result.isSuccess(), "文件上传应该成功: " + result.getMessage());
        assertNotNull(result.getData(), "上传响应应该包含文件数据");
        
        ClientFileDTO uploadedFile = objectMapper.convertValue(result.getData(), ClientFileDTO.class);
        assertNotNull(uploadedFile, "上传的文件DTO不应该为null");
        assertNotNull(uploadedFile.getId(), "上传的文件ID不应该为null");
        String fileId = uploadedFile.getId();

        // 使用无效Token下载应该失败
        mockMvc.perform(get("/api/client/files/{id}/download", fileId)
                        .param("matterId", matterId)
                        .param("token", "invalid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("404"));
    }
}
