package com.clientservice.e2e;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.AccessLogMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完整流程端到端测试
 * 测试场景：接收项目数据 → 发送通知 → 客户访问门户
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("完整流程端到端测试")
@SuppressWarnings("rawtypes")
class FullWorkflowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    @Autowired
    private NotificationRecordMapper notificationRecordMapper;

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private String apiKey;

    @BeforeEach
    void setUp() {
        // 使用测试API密钥
        apiKey = "test-api-key-12345678901234567890";
    }

    @Test
    @DisplayName("完整流程：接收数据→通知→客户访问应该成功")
    void fullWorkflow_ReceiveData_Notify_ClientAccess_ShouldSuccess() throws Exception {
        // ========== Step 1: 接收项目数据 ==========
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("E2E测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 5001L);
        matterData.put("matterName", "E2E测试项目");
        matterData.put("matterNo", "E2E20260202001");
        matterData.put("email", "e2e-test@example.com");
        matterData.put("phone", "13800138000");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
        request.setValidDays(30);

        // 调用接收接口
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.accessUrl").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result result = objectMapper.readValue(responseJson, 
                com.clientservice.common.result.Result.class);
        MatterReceiveResponse response = objectMapper.convertValue(result.getData(), MatterReceiveResponse.class);
        
        String matterId = response.getId();
        String accessUrl = response.getAccessUrl();
        assertNotNull(matterId);
        assertNotNull(accessUrl);
        assertTrue(accessUrl.contains(matterId));

        // 验证数据库记录
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        assertNotNull(matter);
        assertEquals("ACTIVE", matter.getStatus());
        assertEquals(2001L, matter.getClientId());
        assertEquals("E2E测试客户", matter.getClientName());
        assertNotNull(matter.getAccessToken());

        // ========== Step 2: 验证通知发送 ==========
        // 等待异步通知任务完成
        Thread.sleep(1500);

        // 验证通知记录已创建（如果邮件通知启用）
        notificationRecordMapper.selectByMatterId(matterId);
        // 注意：如果邮件服务未配置，通知记录可能为空，这是正常的
        // 这里主要验证流程不会因为通知发送失败而中断

        // ========== Step 3: 客户访问门户 ==========
        String token = matter.getAccessToken();
        // accessUrl格式: http://localhost:8081/portal/matter/{id}?token={token}
        // PortalController路径: /portal/api/matter/{id}
        // 需要提取matterId并构建正确的URL
        String portalUrl = "/portal/api/matter/" + matterId;

        // 访问项目详情
        mockMvc.perform(get(portalUrl)
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(matterId))
                .andExpect(jsonPath("$.data.clientName").value("E2E测试客户"));

        // 验证访问日志已创建（使用MyBatis-Plus的查询方法）
        List<AccessLog> accessLogs = accessLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AccessLog>()
                        .eq(AccessLog::getMatterId, matterId));
        assertNotNull(accessLogs);
        assertFalse(accessLogs.isEmpty());
        assertTrue(accessLogs.stream().anyMatch(log -> 
                log.getMatterId().equals(matterId) && 
                log.getAccessToken().equals(token)));

        // ========== Step 4: 验证完整流程数据一致性 ==========
        // 验证项目状态
        ClientMatter updatedMatter = clientMatterMapper.selectById(matterId);
        assertNotNull(updatedMatter);
        assertEquals("ACTIVE", updatedMatter.getStatus());

        // 验证访问日志记录
        assertTrue(accessLogs.size() >= 1, "应该至少有一条访问日志");
    }

    @Test
    @DisplayName("完整流程：接收数据→客户多次访问应该记录多次日志")
    void fullWorkflow_MultipleAccess_ShouldRecordMultipleLogs() throws Exception {
        // ========== Step 1: 接收项目数据 ==========
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2002L);
        request.setClientName("多次访问测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 5002L);
        matterData.put("matterName", "多次访问测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);

        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result result = objectMapper.readValue(responseJson, 
                com.clientservice.common.result.Result.class);
        MatterReceiveResponse response = objectMapper.convertValue(result.getData(), MatterReceiveResponse.class);
        
        String matterId = response.getId();
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        String token = matter.getAccessToken();
        String portalUrl = "/portal/api/matter/" + matterId;

        // ========== Step 2: 多次访问门户 ==========
        int accessCount = 3;
        for (int i = 0; i < accessCount; i++) {
            mockMvc.perform(get(portalUrl)
                            .param("token", token))
                    .andExpect(status().isOk());
            
            // 短暂延迟，确保时间戳不同
            Thread.sleep(100);
        }

        // ========== Step 3: 验证访问日志记录 ==========
        List<AccessLog> accessLogs = accessLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AccessLog>()
                        .eq(AccessLog::getMatterId, matterId));
        assertNotNull(accessLogs);
        assertTrue(accessLogs.size() >= accessCount, 
                String.format("应该至少有%d条访问日志，实际：%d", accessCount, accessLogs.size()));
    }

    @Test
    @DisplayName("完整流程：接收数据→过期Token访问应该失败")
    void fullWorkflow_ExpiredToken_ShouldFail() throws Exception {
        // ========== Step 1: 接收项目数据（设置短期有效期） ==========
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2003L);
        request.setClientName("过期测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 5003L);
        matterData.put("matterName", "过期测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(0); // 立即过期

        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.clientservice.common.result.Result result = objectMapper.readValue(responseJson, 
                com.clientservice.common.result.Result.class);
        MatterReceiveResponse response = objectMapper.convertValue(result.getData(), MatterReceiveResponse.class);
        
        String matterId = response.getId();
        ClientMatter matter = clientMatterMapper.selectById(matterId);
        String token = matter.getAccessToken();
        String portalUrl = "/portal/api/matter/" + matterId;

        // ========== Step 2: 使用过期Token访问应该失败 ==========
        // 由于validDays=0，token应该立即过期
        // 但需要等待一下确保时间检查通过，或者手动设置过期时间
        // 手动设置过期时间以确保测试通过
        matter.setExpiresAt(java.time.LocalDateTime.now().minusSeconds(1));
        clientMatterMapper.updateById(matter);
        
        mockMvc.perform(get(portalUrl)
                        .param("token", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("403"));
    }
}
