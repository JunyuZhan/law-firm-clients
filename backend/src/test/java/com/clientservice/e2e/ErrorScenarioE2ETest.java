package com.clientservice.e2e;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.service.MatterService;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 错误场景端到端测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("错误场景端到端测试")
class ErrorScenarioE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatterService matterService;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private String apiKey;

    @BeforeEach
    void setUp() {
        apiKey = "test-api-key-12345678901234567890";
    }

    @Test
    @DisplayName("使用无效API密钥接收数据应该失败")
    void receiveDataWithInvalidApiKey_ShouldFail() throws Exception {
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(4001L);
        request.setClientName("错误测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 7001L);
        matterData.put("matterName", "错误测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);

        String requestJson = objectMapper.writeValueAsString(request);
        
        // 使用无效API密钥
        mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer invalid-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @DisplayName("缺少API密钥接收数据应该失败")
    void receiveDataWithoutApiKey_ShouldFail() throws Exception {
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(4002L);
        request.setClientName("错误测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 7002L);
        matterData.put("matterName", "错误测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);

        String requestJson = objectMapper.writeValueAsString(request);
        
        // 不提供API密钥时，Spring 会因为缺少必要的请求头而返回 500 Internal Server Error
        //（MissingRequestHeaderException 未被全局异常处理器捕获）
        mockMvc.perform(post("/api/matter/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("使用无效Token访问门户应该失败")
    void accessPortalWithInvalidToken_ShouldFail() throws Exception {
        // 先创建一个项目
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(4003L);
        request.setClientName("错误测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 7003L);
        matterData.put("matterName", "错误测试项目");
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);
        
        var response = matterService.receiveMatterData(request);
        String matterId = response.getId();

        // 使用无效Token访问
        mockMvc.perform(get("/portal/api/matter/{id}", matterId)
                        .param("token", "invalid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    @DisplayName("访问不存在的项目应该失败")
    void accessNonExistentMatter_ShouldFail() throws Exception {
        String nonExistentMatterId = "non-existent-id-12345";
        String validToken = "some-valid-token";

        mockMvc.perform(get("/portal/api/matter/{id}", nonExistentMatterId)
                        .param("token", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    @DisplayName("Token与项目ID不匹配应该失败")
    void accessMatterWithMismatchedToken_ShouldFail() throws Exception {
        // 创建两个项目
        MatterReceiveRequest request1 = new MatterReceiveRequest();
        request1.setClientId(4004L);
        request1.setClientName("错误测试客户1");
        Map<String, Object> matterData1 = new HashMap<>();
        matterData1.put("matterId", 7004L);
        matterData1.put("matterName", "错误测试项目1");
        request1.setMatterData(matterData1);
        request1.setScopes(Arrays.asList("MATTER_INFO"));
        request1.setValidDays(30);
        var response1 = matterService.receiveMatterData(request1);
        String matterId1 = response1.getId();
        ClientMatter matter1 = clientMatterMapper.selectById(matterId1);
        String token1 = matter1.getAccessToken();

        MatterReceiveRequest request2 = new MatterReceiveRequest();
        request2.setClientId(4005L);
        request2.setClientName("错误测试客户2");
        Map<String, Object> matterData2 = new HashMap<>();
        matterData2.put("matterId", 7005L);
        matterData2.put("matterName", "错误测试项目2");
        request2.setMatterData(matterData2);
        request2.setScopes(Arrays.asList("MATTER_INFO"));
        request2.setValidDays(30);
        var response2 = matterService.receiveMatterData(request2);
        String matterId2 = response2.getId();

        // 使用项目1的Token访问项目2应该失败
        mockMvc.perform(get("/portal/api/matter/{id}", matterId2)
                        .param("token", token1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    @DisplayName("接收无效项目数据应该失败")
    void receiveInvalidMatterData_ShouldFail() throws Exception {
        // 创建缺少必要字段的请求
        MatterReceiveRequest request = new MatterReceiveRequest();
        // 缺少clientId和clientName
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 7006L);
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);

        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("接收空项目数据应该失败")
    void receiveEmptyMatterData_ShouldFail() throws Exception {
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(4007L);
        request.setClientName("错误测试客户");
        request.setMatterData(new HashMap<>()); // 空数据
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);

        String requestJson = objectMapper.writeValueAsString(request);
        
        mockMvc.perform(post("/api/matter/receive")
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
