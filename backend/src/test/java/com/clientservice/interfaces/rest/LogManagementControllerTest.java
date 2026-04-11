package com.clientservice.interfaces.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.AdminLoginLogService;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.clientservice.infrastructure.persistence.mapper.DownloadLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogManagementController 单元测试")
class LogManagementControllerTest {

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @Mock
    private AdminLoginLogService adminLoginLogService;

    @Mock
    private AdminLoginLogMapper adminLoginLogMapper;

    @Mock
    private DownloadLogMapper downloadLogMapper;

    @Test
    @DisplayName("查询登录日志应先校验超级管理员权限")
    void getLoginLogs_ShouldRequireSuperAdmin() {
        LogManagementController controller = new LogManagementController(
                adminAuthorizationService,
                adminLoginLogService,
                adminLoginLogMapper,
                downloadLogMapper
        );
        @SuppressWarnings("unchecked")
        IPage<AdminLoginLog> page = mock(IPage.class);
        doNothing().when(adminAuthorizationService).requireSuperAdmin();
        when(adminLoginLogService.getLoginHistory(null, null, null, 1, 20)).thenReturn(page);

        Result<IPage<AdminLoginLog>> result = controller.getLoginLogs(null, null, null, 1, 20);

        assertTrue(result.isSuccess());
        verify(adminAuthorizationService, times(1)).requireSuperAdmin();
        verify(adminLoginLogService, times(1)).getLoginHistory(null, null, null, 1, 20);
    }
}
