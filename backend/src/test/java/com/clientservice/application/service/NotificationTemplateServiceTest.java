package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationTemplateDTO;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationTemplate;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationTemplateService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationTemplateService 单元测试")
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateMapper templateMapper;

    @InjectMocks
    private NotificationTemplateService notificationTemplateService;

    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        template = NotificationTemplate.builder()
                .id(1L)
                .templateName("测试模板")
                .templateType("SMS")
                .templateCode("SMS_VERIFICATION")
                .templateContent("您的验证码是：{code}")
                .templateVariables("code:验证码")
                .provider("ALIYUN")
                .signName("律所服务")
                .enabled(true)
                .description("短信验证码模板")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    private Page<NotificationTemplate> anyTemplatePage() {
        return org.mockito.ArgumentMatchers.<Page<NotificationTemplate>>any();
    }

    private LambdaQueryWrapper<NotificationTemplate> anyTemplateQueryWrapper() {
        return org.mockito.ArgumentMatchers.<LambdaQueryWrapper<NotificationTemplate>>any();
    }

    @Nested
    @DisplayName("获取模板列表测试")
    class GetTemplateListTests {

        @Test
        @DisplayName("获取模板列表成功 - 无过滤条件")
        void getTemplateList_NoFilters_ShouldReturnAllTemplates() {
            Page<NotificationTemplate> page = new Page<>(1, 100);
            page.setRecords(Collections.singletonList(template));
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试模板", result.get(0).getTemplateName());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }

        @Test
        @DisplayName("获取模板列表成功 - 带类型过滤")
        void getTemplateList_WithTypeFilter_ShouldReturnFilteredTemplates() {
            Page<NotificationTemplate> page = new Page<>(1, 100);
            page.setRecords(Collections.singletonList(template));
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    "SMS", null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }

        @Test
        @DisplayName("获取模板列表成功 - 带提供商过滤")
        void getTemplateList_WithProviderFilter_ShouldReturnFilteredTemplates() {
            Page<NotificationTemplate> page = new Page<>(1, 100);
            page.setRecords(Collections.singletonList(template));
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    null, "ALIYUN", null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }

        @Test
        @DisplayName("获取模板列表成功 - 带启用状态过滤")
        void getTemplateList_WithEnabledFilter_ShouldReturnFilteredTemplates() {
            Page<NotificationTemplate> page = new Page<>(1, 100);
            page.setRecords(Collections.singletonList(template));
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    null, null, true, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }

        @Test
        @DisplayName("获取模板列表成功 - 带数量限制")
        void getTemplateList_WithLimit_ShouldReturnLimitedTemplates() {
            Page<NotificationTemplate> page = new Page<>(1, 50);
            page.setRecords(Collections.singletonList(template));
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    null, null, null, 50);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }

        @Test
        @DisplayName("获取模板列表成功 - 空结果")
        void getTemplateList_NoTemplates_ShouldReturnEmptyList() {
            Page<NotificationTemplate> page = new Page<>(1, 100);
            page.setRecords(Collections.emptyList());
            page.setSearchCount(false);

            when(templateMapper.selectPage(anyTemplatePage(), anyTemplateQueryWrapper()))
                    .thenReturn(page);

            List<NotificationTemplateDTO> result = notificationTemplateService.getTemplateList(
                    null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(templateMapper, times(1)).selectPage(anyTemplatePage(), anyTemplateQueryWrapper());
        }
    }

    @Nested
    @DisplayName("根据ID获取模板测试")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("根据ID获取模板成功")
        void getTemplateById_ValidId_ShouldReturnTemplate() {
            when(templateMapper.selectById(1L)).thenReturn(template);

            NotificationTemplateDTO result = notificationTemplateService.getTemplateById(1L);

            assertNotNull(result);
            assertEquals("测试模板", result.getTemplateName());
            assertEquals("SMS", result.getTemplateType());
            verify(templateMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("根据ID获取模板失败 - 模板不存在")
        void getTemplateById_NotFound_ShouldThrowException() {
            when(templateMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.getTemplateById(999L));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(999L);
        }

        @Test
        @DisplayName("根据ID获取模板失败 - 模板已删除")
        void getTemplateById_Deleted_ShouldThrowException() {
            template.setDeleted(true);
            when(templateMapper.selectById(1L)).thenReturn(template);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.getTemplateById(1L));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(1L);
        }
    }

    @Nested
    @DisplayName("创建模板测试")
    class CreateTemplateTests {

        @Test
        @DisplayName("创建模板成功")
        void createTemplate_ValidData_ShouldCreateTemplate() {
            when(templateMapper.selectByTemplateCode("SMS_VERIFICATION", "ALIYUN")).thenReturn(null);
            when(templateMapper.insert(any(NotificationTemplate.class))).thenAnswer(invocation -> {
                NotificationTemplate t = invocation.getArgument(0);
                t.setId(1L);
                return 1;
            });

            NotificationTemplateDTO result = notificationTemplateService.createTemplate(
                    "测试模板", "SMS", "SMS_VERIFICATION", "您的验证码是：{code}",
                    "code:验证码", "ALIYUN", "律所服务", true, "短信验证码模板");

            assertNotNull(result);
            assertEquals("测试模板", result.getTemplateName());
            assertEquals("SMS", result.getTemplateType());
            verify(templateMapper, times(1)).selectByTemplateCode("SMS_VERIFICATION", "ALIYUN");
            verify(templateMapper, times(1)).insert(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("创建模板失败 - 模板名称为空")
        void createTemplate_EmptyName_ShouldThrowException() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.createTemplate(
                            "", "SMS", "SMS_VERIFICATION", "内容",
                            "变量", "ALIYUN", "签名", true, "描述"));

            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("模板名称不能为空"));
            verify(templateMapper, never()).selectByTemplateCode(anyString(), anyString());
            verify(templateMapper, never()).insert(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("创建模板失败 - 模板类型为空")
        void createTemplate_EmptyType_ShouldThrowException() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.createTemplate(
                            "测试模板", "", "SMS_VERIFICATION", "内容",
                            "变量", "ALIYUN", "签名", true, "描述"));

            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("模板类型不能为空"));
            verify(templateMapper, never()).selectByTemplateCode(anyString(), anyString());
            verify(templateMapper, never()).insert(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("创建模板失败 - 模板代码已存在")
        void createTemplate_DuplicateCode_ShouldThrowException() {
            when(templateMapper.selectByTemplateCode("SMS_VERIFICATION", "ALIYUN")).thenReturn(template);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.createTemplate(
                            "测试模板", "SMS", "SMS_VERIFICATION", "内容",
                            "变量", "ALIYUN", "签名", true, "描述"));

            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("该模板代码已存在"));
            verify(templateMapper, times(1)).selectByTemplateCode("SMS_VERIFICATION", "ALIYUN");
            verify(templateMapper, never()).insert(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("创建模板成功 - 无模板代码和提供商")
        void createTemplate_NoCodeNoProvider_ShouldCreateTemplate() {
            when(templateMapper.insert(any(NotificationTemplate.class))).thenAnswer(invocation -> {
                NotificationTemplate t = invocation.getArgument(0);
                t.setId(1L);
                return 1;
            });

            NotificationTemplateDTO result = notificationTemplateService.createTemplate(
                    "测试模板", "SMS", null, "您的验证码是：{code}",
                    "code:验证码", null, null, true, "短信验证码模板");

            assertNotNull(result);
            assertEquals("测试模板", result.getTemplateName());
            verify(templateMapper, never()).selectByTemplateCode(anyString(), anyString());
            verify(templateMapper, times(1)).insert(any(NotificationTemplate.class));
        }
    }

    @Nested
    @DisplayName("更新模板测试")
    class UpdateTemplateTests {

        @Test
        @DisplayName("更新模板成功 - 更新所有字段")
        void updateTemplate_AllFields_ShouldUpdateTemplate() {
            when(templateMapper.selectById(1L)).thenReturn(template);
            when(templateMapper.updateById(any(NotificationTemplate.class))).thenReturn(1);

            NotificationTemplateDTO result = notificationTemplateService.updateTemplate(
                    1L, "新模板名", "新内容", "新变量", false, "新描述");

            assertNotNull(result);
            assertEquals("新模板名", result.getTemplateName());
            verify(templateMapper, times(1)).selectById(1L);
            verify(templateMapper, times(1)).updateById(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("更新模板成功 - 部分字段更新")
        void updateTemplate_PartialFields_ShouldUpdateTemplate() {
            when(templateMapper.selectById(1L)).thenReturn(template);
            when(templateMapper.updateById(any(NotificationTemplate.class))).thenReturn(1);

            NotificationTemplateDTO result = notificationTemplateService.updateTemplate(
                    1L, null, "新内容", null, null, null);

            assertNotNull(result);
            assertEquals("测试模板", result.getTemplateName()); // 名称未变
            verify(templateMapper, times(1)).selectById(1L);
            verify(templateMapper, times(1)).updateById(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("更新模板失败 - 模板不存在")
        void updateTemplate_NotFound_ShouldThrowException() {
            when(templateMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.updateTemplate(
                            999L, "新名称", "新内容", "新变量", true, "新描述"));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(999L);
            verify(templateMapper, never()).updateById(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("更新模板失败 - 模板已删除")
        void updateTemplate_Deleted_ShouldThrowException() {
            template.setDeleted(true);
            when(templateMapper.selectById(1L)).thenReturn(template);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.updateTemplate(
                            1L, "新名称", "新内容", "新变量", true, "新描述"));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(1L);
            verify(templateMapper, never()).updateById(any(NotificationTemplate.class));
        }
    }

    @Nested
    @DisplayName("删除模板测试")
    class DeleteTemplateTests {

        @Test
        @DisplayName("删除模板成功")
        void deleteTemplate_ValidId_ShouldDeleteTemplate() {
            when(templateMapper.selectById(1L)).thenReturn(template);
            when(templateMapper.updateById(any(NotificationTemplate.class))).thenReturn(1);

            assertDoesNotThrow(() -> notificationTemplateService.deleteTemplate(1L));

            assertTrue(template.getDeleted());
            verify(templateMapper, times(1)).selectById(1L);
            verify(templateMapper, times(1)).updateById(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("删除模板失败 - 模板不存在")
        void deleteTemplate_NotFound_ShouldThrowException() {
            when(templateMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.deleteTemplate(999L));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(999L);
            verify(templateMapper, never()).updateById(any(NotificationTemplate.class));
        }

        @Test
        @DisplayName("删除模板失败 - 模板已删除")
        void deleteTemplate_AlreadyDeleted_ShouldThrowException() {
            template.setDeleted(true);
            when(templateMapper.selectById(1L)).thenReturn(template);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> notificationTemplateService.deleteTemplate(1L));

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("模板不存在"));
            verify(templateMapper, times(1)).selectById(1L);
            verify(templateMapper, never()).updateById(any(NotificationTemplate.class));
        }
    }

    @Nested
    @DisplayName("获取启用的模板测试")
    class GetEnabledTemplatesTests {

        @Test
        @DisplayName("获取启用的模板成功")
        void getEnabledTemplatesByTypeAndProvider_ValidParams_ShouldReturnTemplates() {
            List<NotificationTemplate> templates = Collections.singletonList(template);
            when(templateMapper.selectEnabledByTypeAndProvider("SMS", "ALIYUN")).thenReturn(templates);

            List<NotificationTemplateDTO> result = notificationTemplateService
                    .getEnabledTemplatesByTypeAndProvider("SMS", "ALIYUN");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试模板", result.get(0).getTemplateName());
            verify(templateMapper, times(1)).selectEnabledByTypeAndProvider("SMS", "ALIYUN");
        }

        @Test
        @DisplayName("获取启用的模板成功 - 空结果")
        void getEnabledTemplatesByTypeAndProvider_NoTemplates_ShouldReturnEmptyList() {
            when(templateMapper.selectEnabledByTypeAndProvider("EMAIL", "TENCENT"))
                    .thenReturn(Collections.emptyList());

            List<NotificationTemplateDTO> result = notificationTemplateService
                    .getEnabledTemplatesByTypeAndProvider("EMAIL", "TENCENT");

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(templateMapper, times(1)).selectEnabledByTypeAndProvider("EMAIL", "TENCENT");
        }
    }
}
