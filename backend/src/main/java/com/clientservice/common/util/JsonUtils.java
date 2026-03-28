package com.clientservice.common.util;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * JSON工具类
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(final Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "对象转JSON失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 类型参数
     * @return 对象
     */
    public static <T> T fromJson(final String json, final Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: json={}, class={}", json, clazz.getName(), e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "JSON转对象失败: " + e.getMessage());
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param json JSON字符串
     * @return Map
     */
    public static Map<String, Object> toMap(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转Map失败: json={}", json, e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "JSON转Map失败: " + e.getMessage());
        }
    }

    /**
     * 对象转Map
     *
     * @param obj 对象
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(final Object obj) {
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    /**
     * 安全地解析JSON（失败返回null）
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 类型参数
     * @return 对象，失败返回null
     */
    public static <T> T fromJsonSafe(final String json, final Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("JSON转对象失败（安全模式）: json={}, class={}", json, clazz.getName(), e);
            return null;
        }
    }

    /**
     * 安全地解析JSON为Map（失败返回空Map）
     *
     * @param json JSON字符串
     * @return Map，失败返回空Map
     */
    public static Map<String, Object> toMapSafe(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("JSON转Map失败（安全模式）: json={}", json, e);
            return Map.of();
        }
    }
}
