package info.zhihui.ems.components.redis.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyPrefixHandlerTest {

    @Test
    @DisplayName("map-空key原样返回")
    void testMap_BlankKey_ReturnSame() {
        KeyPrefixHandler handler = new KeyPrefixHandler("ems");
        assertNull(handler.map(null));
        assertEquals("", handler.map(""));
    }

    @Test
    @DisplayName("unmap-空key原样返回")
    void testUnmap_BlankKey_ReturnSame() {
        KeyPrefixHandler handler = new KeyPrefixHandler("ems");
        assertNull(handler.unmap(null));
        assertEquals("", handler.unmap(""));
    }
}
