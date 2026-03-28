package com.clientservice.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtils 单元测试
 */
@DisplayName("JsonUtils 单元测试")
@SuppressWarnings("unchecked")
class JsonUtilsTest {

    @Nested
    @DisplayName("JSON序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("对象转JSON字符串")
        void toJson_ShouldReturnJsonString() {
            // Given
            Map<String, Object> data = new HashMap<>();
            data.put("name", "测试");
            data.put("value", 123);

            // When
            String json = JsonUtils.toJson(data);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("name"));
            assertTrue(json.contains("测试"));
            assertTrue(json.contains("value"));
            assertTrue(json.contains("123"));
        }

        @Test
        @DisplayName("包含LocalDateTime的对象序列化")
        void toJson_WithLocalDateTime_ShouldSerializeCorrectly() {
            // Given
            Map<String, Object> data = new HashMap<>();
            data.put("time", LocalDateTime.of(2026, 2, 2, 10, 30, 0));

            // When
            String json = JsonUtils.toJson(data);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("2026-02-02"));
            assertTrue(json.contains("10:30:00"));
        }

        @Test
        @DisplayName("null值序列化")
        void toJson_WithNull_ShouldReturnNullString() {
            // When
            String json = JsonUtils.toJson(null);

            // Then
            assertEquals("null", json);
        }
    }

    @Nested
    @DisplayName("JSON反序列化测试")
    class DeserializationTests {

        @Test
        @DisplayName("JSON字符串转Map")
        void fromJson_ShouldReturnMap() {
            // Given
            String json = "{\"name\":\"测试\",\"value\":123}";

            // When
            Map<String, Object> result = JsonUtils.fromJson(json, Map.class);

            // Then
            assertNotNull(result);
            assertEquals("测试", result.get("name"));
            assertEquals(123, result.get("value"));
        }

        @Test
        @DisplayName("JSON字符串转对象")
        void fromJson_ShouldReturnObject() {
            // Given
            String json = "{\"name\":\"测试\",\"value\":123}";

            // When
            TestData result = JsonUtils.fromJson(json, TestData.class);

            // Then
            assertNotNull(result);
            assertEquals("测试", result.name);
            assertEquals(123, result.value);
        }

        @Test
        @DisplayName("无效JSON应该返回null（安全方法）")
        void fromJsonSafe_WithInvalidJson_ShouldReturnNull() {
            // Given
            String invalidJson = "{invalid json}";

            // When
            Map<String, Object> result = JsonUtils.fromJsonSafe(invalidJson, Map.class);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Map转换测试")
    class MapConversionTests {

        @Test
        @DisplayName("对象转Map")
        void toMap_ShouldReturnMap() {
            // Given
            TestData data = new TestData();
            data.name = "测试";
            data.value = 123;

            // When
            Map<String, Object> result = JsonUtils.toMap(data);

            // Then
            assertNotNull(result);
            assertEquals("测试", result.get("name"));
            assertEquals(123, result.get("value"));
        }

        @Test
        @DisplayName("无效JSON字符串转Map应该返回空Map（安全方法）")
        void toMapSafe_WithInvalidJson_ShouldReturnEmptyMap() {
            // Given
            String invalidJson = "invalid json string";

            // When
            Map<String, Object> result = JsonUtils.toMapSafe(invalidJson);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // 测试数据类
    static class TestData {
        public String name;
        public Integer value;
    }
}
