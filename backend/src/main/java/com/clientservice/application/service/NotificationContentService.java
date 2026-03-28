package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationTemplate;
import com.clientservice.application.util.TemplateVariableReplacer;
import com.clientservice.domain.entity.ClientMatter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationContentService {

    private final ObjectMapper objectMapper;

    public NotificationTemplate buildNotificationTemplate(final ClientMatter matter) {
        return NotificationTemplate.builder()
                .matterName(matter.getClientName() + "的项目")
                .clientName(matter.getClientName())
                .accessUrl(matter.getAccessUrl())
                .validDays(matter.getValidDays())
                .build();
    }

    public String buildEmailContent(
            final NotificationTemplate template,
            final com.clientservice.domain.entity.NotificationTemplate emailTemplate) {
        if (emailTemplate != null
                && emailTemplate.getTemplateContent() != null
                && !emailTemplate.getTemplateContent().isEmpty()) {
            try {
                return TemplateVariableReplacer.replaceVariables(emailTemplate.getTemplateContent(), template);
            } catch (Exception e) {
                log.warn("使用邮件模板内容失败，使用默认格式", e);
            }
        }

        return TemplateVariableReplacer.buildDefaultEmailContent(template);
    }

    public String buildWechatTemplateData(final String templateContent, final NotificationTemplate template) {
        if (templateContent == null || templateContent.isEmpty()) {
            return buildDefaultWechatTemplateData(template);
        }

        try {
            Map<String, Object> templateMap = objectMapper.readValue(
                    templateContent,
                    new TypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> resultMap = replaceVariablesInMap(templateMap, template);
            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            log.warn("解析模板内容失败，使用默认格式", e);
            return buildDefaultWechatTemplateData(template);
        }
    }

    private String buildDefaultWechatTemplateData(final NotificationTemplate template) {
        return String.format(
                "{\"first\":{\"value\":\"项目信息已更新\"},\"keyword1\":{\"value\":\"%s\"},\"keyword2\":{\"value\":\"%s\"},\"remark\":{\"value\":\"点击查看详情\"}}",
                template.getMatterName() != null ? template.getMatterName() : "",
                template.getAccessUrl() != null ? template.getAccessUrl() : "");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> replaceVariablesInMap(
            final Map<String, Object> map,
            final NotificationTemplate template) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.put(entry.getKey(), replaceVariablesInMap((Map<String, Object>) value, template));
            } else if (value instanceof String) {
                result.put(entry.getKey(), TemplateVariableReplacer.replaceVariables((String) value, template));
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }
}
