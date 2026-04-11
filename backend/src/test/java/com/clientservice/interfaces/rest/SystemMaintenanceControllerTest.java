package com.clientservice.interfaces.rest;

import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SystemMaintenanceController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SystemMaintenanceController 单元测试")
@SuppressWarnings("unchecked")
class SystemMaintenanceControllerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @InjectMocks
    private SystemMaintenanceController systemMaintenanceController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(systemMaintenanceController, "fileStoragePath", "/tmp/client-service/files");
        ReflectionTestUtils.setField(systemMaintenanceController, "datasourceUrl", "jdbc:postgresql://localhost:5432/client_service");
        ReflectionTestUtils.setField(systemMaintenanceController, "currentVersion", "1.0.0");
        ReflectionTestUtils.setField(systemMaintenanceController, "githubRepo", "test/repo");
        ReflectionTestUtils.setField(systemMaintenanceController, "backupDirectory", "/tmp/client-service-backups");
    }

    @Nested
    @DisplayName("系统状态测试")
    class SystemStatusTests {

        @Test
        @DisplayName("获取系统状态应该成功")
        void getSystemStatus_ShouldSuccess() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);
            when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(*)"), eq(Long.class))).thenReturn(10L);
            when(jdbcTemplate.queryForObject(eq("SELECT pg_database_size(?)"), eq(Long.class), anyString())).thenReturn(1024L * 1024L * 10L);

            // When
            Result<Map<String, Object>> result = systemMaintenanceController.getSystemStatus();

            // Then
            assertTrue(result.isSuccess());
            Map<String, Object> data = result.getData();
            assertNotNull(data.get("database"));
            assertNotNull(data.get("storage"));
            assertNotNull(data.get("system"));
            
            Map<String, Object> dbStatus = (Map<String, Object>) data.get("database");
            assertTrue((Boolean) dbStatus.get("connected"));
        }

        @Test
        @DisplayName("数据库连接失败应该返回错误状态")
        void getSystemStatus_WithDbFailure_ShouldReturnError() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenThrow(new RuntimeException("Connection failed"));

            // When
            Result<Map<String, Object>> result = systemMaintenanceController.getSystemStatus();

            // Then
            assertTrue(result.isSuccess());
            Map<String, Object> data = result.getData();
            Map<String, Object> dbStatus = (Map<String, Object>) data.get("database");
            assertFalse((Boolean) dbStatus.get("connected"));
            assertNotNull(dbStatus.get("error"));
        }
    }

    @Nested
    @DisplayName("数据库备份测试")
    class BackupTests {

        @Test
        @DisplayName("创建数据库备份应该成功")
        void createDatabaseBackup_ShouldSuccess() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            when(jdbcTemplate.queryForList(anyString())).thenReturn(java.util.Collections.emptyList());

            // When
            Result<Map<String, Object>> result = systemMaintenanceController.createDatabaseBackup();

            // Then
            assertTrue(result.isSuccess());
            Map<String, Object> data = result.getData();
            assertTrue((Boolean) data.get("success"));
            assertNotNull(data.get("filename"));
        }
    }

    @Nested
    @DisplayName("版本检查测试")
    class VersionCheckTests {

        @Test
        @DisplayName("获取Git信息应该成功")
        void getGitInfo_ShouldSuccess() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            Map<String, Object> releaseInfo = new HashMap<>();
            releaseInfo.put("tag_name", "v1.1.0");
            releaseInfo.put("body", "Release notes");
            releaseInfo.put("html_url", "http://github.com/test/repo/releases/v1.1.0");
            releaseInfo.put("published_at", "2023-01-01T00:00:00Z");
            
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(releaseInfo);

            // When
            Result<Map<String, Object>> result = systemMaintenanceController.getGitInfo();

            // Then
            assertTrue(result.isSuccess());
            Map<String, Object> data = result.getData();
            assertEquals("1.0.0", data.get("currentVersion"));
            assertEquals("1.1.0", data.get("remoteVersion"));
            assertTrue((Boolean) data.get("hasUpdate"));
        }

        @Test
        @DisplayName("版本检查应该返回是否有更新")
        void checkVersion_ShouldReturnUpdateStatus() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            Map<String, Object> releaseInfo = new HashMap<>();
            releaseInfo.put("tag_name", "v1.1.0");
            
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(releaseInfo);

            // When
            Result<Map<String, Object>> result = systemMaintenanceController.checkVersion();

            // Then
            assertTrue(result.isSuccess());
            Map<String, Object> data = result.getData();
            assertEquals("1.1.0", data.get("latestVersion"));
            assertTrue((Boolean) data.get("hasUpdate"));
        }
        
        @Test
        @DisplayName("忽略版本应该成功")
        void ignoreVersion_ShouldSuccess() {
            // Given
            doNothing().when(adminAuthorizationService).requireSuperAdmin();
            String version = "1.1.0";
            // Mock ignoreVersion calls
             when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class), anyString())).thenReturn(0L);
             when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
             
             // Mock checkVersion calls
            when(jdbcTemplate.queryForObject(contains("SELECT config_value"), eq(String.class))).thenReturn(version);
            
            // When
            Result<Void> result = systemMaintenanceController.ignoreVersion(version);
            
            // Then
            assertTrue(result.isSuccess());
            // 再次获取应该被标记为已忽略
            Result<Map<String, Object>> checkResult = systemMaintenanceController.checkVersion();
            assertEquals(version, checkResult.getData().get("ignoredVersion"));
        }
    }
}
