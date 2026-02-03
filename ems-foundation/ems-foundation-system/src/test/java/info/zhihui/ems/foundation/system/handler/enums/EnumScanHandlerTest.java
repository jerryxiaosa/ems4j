package info.zhihui.ems.foundation.system.handler.enums;

import com.google.common.cache.Cache;
import info.zhihui.ems.foundation.system.dto.EnumItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("枚举扫描处理器测试")
class EnumScanHandlerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("getAll返回Map与List均不可修改")
    void testGetAll_ReturnsUnmodifiableMapAndList() {
        EnumScanHandler handler = new EnumScanHandler(applicationContext);

        @SuppressWarnings("unchecked")
        Cache<String, Map<String, List<EnumItemDto>>> cache =
                (Cache<String, Map<String, List<EnumItemDto>>>) ReflectionTestUtils.getField(handler, "cache");

        Map<String, List<EnumItemDto>> source = new HashMap<>();
        List<EnumItemDto> items = new ArrayList<>();
        items.add(new EnumItemDto(1, "one"));
        source.put("test", items);
        cache.put("ENUM_ALL", source);

        Map<String, List<EnumItemDto>> result = handler.getAll();

        assertThrows(UnsupportedOperationException.class, () -> result.put("x", List.of()));
        assertThrows(UnsupportedOperationException.class, () -> result.get("test").add(new EnumItemDto(2, "two")));
    }
}
