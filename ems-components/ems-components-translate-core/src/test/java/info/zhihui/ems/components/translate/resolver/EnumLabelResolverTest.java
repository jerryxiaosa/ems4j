package info.zhihui.ems.components.translate.resolver;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.components.translate.engine.TranslateContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EnumLabelResolver测试")
class EnumLabelResolverTest {

    private final EnumLabelResolver resolver = new EnumLabelResolver();

    @Test
    @DisplayName("空参数时应返回空映射")
    void testResolveBatch_EmptyArgs_ShouldReturnEmptyMap() {
        assertTrue(resolver.resolveBatch(null, TestCodeEnum.class, new TranslateContext()).isEmpty());
        assertTrue(resolver.resolveBatch(Set.of(), TestCodeEnum.class, new TranslateContext()).isEmpty());
        assertTrue(resolver.resolveBatch(Set.of(1), null, new TranslateContext()).isEmpty());
    }

    @Test
    @DisplayName("未实现CodeEnum的枚举应返回空映射")
    void testResolveBatch_NonCodeEnum_ShouldReturnEmptyMap() {
        Map<Object, String> result = resolver.resolveBatch(Set.of("A"), PlainEnum.class, new TranslateContext());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("空枚举应返回空映射")
    void testResolveBatch_EmptyEnum_ShouldReturnEmptyMap() {
        Map<Object, String> result = resolver.resolveBatch(Set.of("A"), EmptyEnum.class, new TranslateContext());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("应仅返回命中的枚举显示值")
    void testResolveBatch_ShouldReturnMatchedLabelsOnly() {
        Set<Object> keys = new LinkedHashSet<>();
        keys.add(1);
        keys.add(999);
        keys.add(0);
        keys.add(null);

        Map<Object, String> result = resolver.resolveBatch(keys, TestCodeEnum.class, new TranslateContext());

        assertEquals(2, result.size());
        assertEquals("启用", result.get(1));
        assertEquals("停用", result.get(0));
    }

    private enum TestCodeEnum implements CodeEnum<Integer> {
        DISABLED(0, "停用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String info;

        TestCodeEnum(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getInfo() {
            return info;
        }
    }

    private enum PlainEnum {
        A,
        B
    }

    private enum EmptyEnum {
    }
}
