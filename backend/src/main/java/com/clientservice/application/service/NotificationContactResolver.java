package com.clientservice.application.service;

import com.clientservice.application.dto.ContactInfoExtractResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationContactResolver {

    private final ObjectMapper objectMapper;

    public Map<String, Object> parseMatterData(final String matterDataJson) {
        if (matterDataJson == null || matterDataJson.trim().isEmpty()) {
            log.warn("项目数据为空，无法解析");
            return Map.of();
        }

        try {
            Map<String, Object> result = objectMapper.readValue(
                    matterDataJson,
                    new TypeReference<Map<String, Object>>() {
                    });
            if (result == null || result.isEmpty()) {
                log.warn("项目数据解析结果为空: {}", matterDataJson);
                return Map.of();
            }
            return result;
        } catch (Exception e) {
            log.error("解析项目数据失败，JSON格式可能不正确: {}",
                    matterDataJson.length() > 200 ? matterDataJson.substring(0, 200) + "..." : matterDataJson, e);
            return Map.of();
        }
    }

    public ContactInfoExtractResult extractPhoneNumber(final Map<String, Object> matterData) {
        return extractField(matterData, "phone", "phoneNumber", "mobile", "mobilePhone", "contactPhone");
    }

    public ContactInfoExtractResult extractEmail(final Map<String, Object> matterData) {
        return extractField(matterData, "email", "emailAddress", "contactEmail");
    }

    public ContactInfoExtractResult extractWechatOpenId(final Map<String, Object> matterData) {
        return extractField(matterData, "wechatOpenId", "openId", "wechatId");
    }

    private ContactInfoExtractResult extractField(final Map<String, Object> matterData, final String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = matterData.get(fieldName);
            if (value != null) {
                String strValue = value.toString().trim();
                if (!strValue.isEmpty()) {
                    return ContactInfoExtractResult.builder()
                            .value(strValue)
                            .searchedFieldNames(fieldNames)
                            .build();
                }
            }
        }
        return ContactInfoExtractResult.builder()
                .value(null)
                .searchedFieldNames(fieldNames)
                .build();
    }
}
