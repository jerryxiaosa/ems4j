package info.zhihui.ems.business.device.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeviceUtil单元测试
 *
 * @author jerryxiaosa
 */
class DeviceUtilTest {

    @Test
    @DisplayName("getProperty - 正常获取String类型属性")
    void getProperty_shouldReturnStringValue_whenValidInput() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("voltage", "220V");

        // When
        String result = DeviceUtil.getProperty(properties, "voltage", String.class);

        // Then
        assertEquals("220V", result);
    }

    @Test
    @DisplayName("getProperty - 正常获取Integer类型属性")
    void getProperty_shouldReturnIntegerValue_whenValidInput() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("power", 100);

        // When
        Integer result = DeviceUtil.getProperty(properties, "power", Integer.class);

        // Then
        assertEquals(100, result);
    }

    @Test
    @DisplayName("getProperty - 正常获取Boolean类型属性")
    void getProperty_shouldReturnBooleanValue_whenValidInput() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("enabled", true);

        // When
        Boolean result = DeviceUtil.getProperty(properties, "enabled", Boolean.class);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("getProperty - 当properties为null时返回null")
    void getProperty_shouldReturnNull_whenPropertiesIsNull() {
        // Given
        Map<String, Object> properties = null;

        // When
        String result = DeviceUtil.getProperty(properties, "voltage", String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("getProperty - 当key不存在时返回null")
    void getProperty_shouldReturnNull_whenKeyNotExists() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("voltage", "220V");

        // When
        String result = DeviceUtil.getProperty(properties, "nonExistentKey", String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("getProperty - 当key对应的值为null时返回null")
    void getProperty_shouldReturnNull_whenValueIsNull() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("voltage", null);

        // When
        String result = DeviceUtil.getProperty(properties, "voltage", String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("getProperty - 类型转换异常测试")
    void getProperty_shouldThrowClassCastException_whenTypeMismatch() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("voltage", "220V"); // String类型的值

        // When & Then
        assertThrows(ClassCastException.class, () -> {
            DeviceUtil.getProperty(properties, "voltage", Integer.class); // 尝试转换为Integer
        });
    }

    @Test
    @DisplayName("getProperty - 获取复杂对象类型属性")
    void getProperty_shouldReturnComplexObject_whenValidInput() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("port", "8080");
        properties.put("config", config);

        // When
        @SuppressWarnings("unchecked")
        Map<String, String> result = DeviceUtil.getProperty(properties, "config", Map.class);

        // Then
        assertNotNull(result);
        assertEquals("localhost", result.get("host"));
        assertEquals("8080", result.get("port"));
    }

    @Test
    @DisplayName("getProperty - 获取Double类型属性")
    void getProperty_shouldReturnDoubleValue_whenValidInput() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("temperature", 25.5);

        // When
        Double result = DeviceUtil.getProperty(properties, "temperature", Double.class);

        // Then
        assertEquals(25.5, result);
    }

    @Test
    @DisplayName("getProperty - 空字符串key测试")
    void getProperty_shouldReturnNull_whenKeyIsEmpty() {
        // Given
        Map<String, Object> properties = new HashMap<>();
        properties.put("voltage", "220V");

        // When
        String result = DeviceUtil.getProperty(properties, "", String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("getProperty - 空Map测试")
    void getProperty_shouldReturnNull_whenMapIsEmpty() {
        // Given
        Map<String, Object> properties = new HashMap<>();

        // When
        String result = DeviceUtil.getProperty(properties, "voltage", String.class);

        // Then
        assertNull(result);
    }
}