package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalUtilTest {

    @Test
    void testPut_NonSerializable_ShouldThrow() {
        Object value = new Object();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ThreadLocalUtil.put("key", value));
        assertEquals("ThreadLocal value must implement Serializable", exception.getMessage());
    }

    @Test
    void testPut_Serializable_ShouldStore() {
        Serializable value = "test";

        assertDoesNotThrow(() -> ThreadLocalUtil.put("key", value));
        assertEquals(value, ThreadLocalUtil.get("key"));
        ThreadLocalUtil.remove("key");
    }

    @Test
    void testPut_Null_ShouldStore() {
        assertDoesNotThrow(() -> ThreadLocalUtil.put("key", null));
        assertNull(ThreadLocalUtil.get("key"));
        ThreadLocalUtil.remove("key");
    }
}
