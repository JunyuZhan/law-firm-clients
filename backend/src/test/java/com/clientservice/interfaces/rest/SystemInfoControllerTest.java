package com.clientservice.interfaces.rest;

import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemInfoController 单元测试")
class SystemInfoControllerTest {

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @Mock
    private ObjectProvider<BuildProperties> buildPropertiesProvider;

    @Mock
    private ObjectProvider<HealthEndpoint> healthEndpointProvider;

    private SystemInfoController controller;

    @BeforeEach
    void setUp() {
        controller = new SystemInfoController(
                adminAuthorizationService,
                buildPropertiesProvider,
                healthEndpointProvider
        );
        ReflectionTestUtils.setField(controller, "applicationName", "client-service");
        ReflectionTestUtils.setField(controller, "productVersion", "1.0.0");
        ReflectionTestUtils.setField(controller, "commitSha", "abc123");
        ReflectionTestUtils.setField(controller, "buildTime", "2026-04-11T18:00:00");
        ReflectionTestUtils.setField(controller, "recommendedMode", "Docker Compose");
        ReflectionTestUtils.setField(controller, "fileStoragePath", "/tmp/client-service/files");
    }

    @Test
    @DisplayName("查询运行时信息应先校验超级管理员权限")
    void getRuntimeInfo_ShouldRequireSuperAdmin() {
        doNothing().when(adminAuthorizationService).requireSuperAdmin();

        Result<Map<String, Object>> result = controller.getRuntimeInfo();

        assertTrue(result.isSuccess());
        verify(adminAuthorizationService, times(1)).requireSuperAdmin();
    }
}
