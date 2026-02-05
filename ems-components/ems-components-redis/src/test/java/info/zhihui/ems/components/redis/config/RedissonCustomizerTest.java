package info.zhihui.ems.components.redis.config;

import com.example.redis.ForbiddenPayload;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import info.zhihui.ems.components.redis.properties.RedissonProperties;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.config.Config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class RedissonCustomizerTest {

    @Test
    @DisplayName("customize-允许白名单类型反序列化")
    void testCustomize_AllowsWhitelistedType() throws IOException {
        Codec codec = buildCodec(true);
        AllowedPayload payload = new AllowedPayload("ok");

        Object decoded = encodeDecode(codec, payload);

        assertInstanceOf(AllowedPayload.class, decoded);
        assertEquals(payload, decoded);
    }

    @Test
    @DisplayName("customize-禁止非白名单类型反序列化")
    void testCustomize_DisallowNonWhitelistedType() {
        Codec codec = buildCodec(true);
        ForbiddenPayload payload = new ForbiddenPayload("bad");

        assertThrows(InvalidTypeIdException.class, () -> encodeDecode(codec, payload));
    }

    @Test
    @DisplayName("customize-未启用白名单时允许非白名单类型")
    void testCustomize_DefaultAllowAll() throws IOException {
        Codec codec = buildCodec(false);
        ForbiddenPayload payload = new ForbiddenPayload("ok");

        Object decoded = encodeDecode(codec, payload);

        assertInstanceOf(ForbiddenPayload.class, decoded);
        assertEquals("ok", ((ForbiddenPayload) decoded).getValue());
    }

    private Codec buildCodec(boolean enableAllowlist) {
        RedissonProperties properties = new RedissonProperties();
        properties.setAllowlistEnabled(enableAllowlist);
        properties.setAllowlistPackages(List.of("info.zhihui.ems", "cn.dev33.satoken", "java.util", "java.lang", "java.time", "java.math"));
        RedissonCustomizer customizer = new RedissonCustomizer(properties);
        Config config = new Config();
        customizer.customize(config);
        return config.getCodec();
    }

    private Object encodeDecode(Codec codec, Object value) throws IOException {
        ByteBuf byteBuf = codec.getValueEncoder().encode(value);
        try {
            return codec.getValueDecoder().decode(byteBuf, new State());
        } finally {
            byteBuf.release();
        }
    }

    static class AllowedPayload {
        private String value;

        public AllowedPayload() {
        }

        public AllowedPayload(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AllowedPayload that = (AllowedPayload) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
