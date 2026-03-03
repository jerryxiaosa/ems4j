package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class GatewayNameResolverIntegrationTest {

    @Autowired
    private GatewayNameResolver gatewayNameResolver;

    @Test
    @DisplayName("批量解析应返回网关名称映射")
    void testResolveBatch_ValidKeys_ShouldReturnNameMap() {
        Set<Integer> keySet = new LinkedHashSet<>();
        keySet.add(1);
        keySet.add(null);
        keySet.add(999999999);

        Map<Integer, String> resultMap = gatewayNameResolver.resolveBatch(keySet, new TranslateContext());

        assertFalse(resultMap.isEmpty());
        assertTrue(resultMap.containsKey(1));
        assertFalse(resultMap.containsKey(null));
        assertFalse(resultMap.containsKey(999999999));
    }

    @Test
    @DisplayName("空入参应返回空映射")
    void testResolveBatch_EmptyOrNullKeys_ShouldReturnEmptyMap() {
        assertTrue(gatewayNameResolver.resolveBatch(Collections.emptySet(), new TranslateContext()).isEmpty());
        assertTrue(gatewayNameResolver.resolveBatch(null, new TranslateContext()).isEmpty());
    }
}
