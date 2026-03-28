package com.clientservice.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HealthController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController 单元测试")
class HealthControllerTest {

    private MockMvc mockMvc;

    @Test
    @DisplayName("健康检查接口应该返回成功")
    void health_ShouldReturnSuccess() throws Exception {
        // Given
        HealthController controller = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("就绪检查接口应该返回成功")
    void ready_ShouldReturnSuccess() throws Exception {
        // Given
        HealthController controller = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // When & Then
        mockMvc.perform(get("/api/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    @DisplayName("存活检查接口应该返回成功")
    void live_ShouldReturnSuccess() throws Exception {
        // Given
        HealthController controller = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // When & Then
        mockMvc.perform(get("/api/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ALIVE"));
    }
}
