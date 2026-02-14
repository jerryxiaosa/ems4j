package info.zhihui.ems.web.account;

import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.web.account.resolver.WarnPlanNameResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class WarnPlanNameResolverIntegrationTest {

    @Autowired
    private WarnPlanNameResolver warnPlanNameResolver;

    @Autowired
    private WarnPlanService warnPlanService;

    @Test
    @DisplayName("批量解析应返回预警方案名称映射")
    void testResolveBatch_ValidKeys_ShouldReturnNameMap() {
        String planName = "resolver-plan-" + UUID.randomUUID();
        Integer planId = warnPlanService.add(buildWarnPlanSaveDto(planName));

        Set<Integer> keySet = new LinkedHashSet<>();
        keySet.add(planId);
        keySet.add(null);
        keySet.add(999999999);

        Map<Integer, String> resultMap = warnPlanNameResolver.resolveBatch(keySet, new TranslateContext());

        assertEquals(planName, resultMap.get(planId));
        assertFalse(resultMap.containsKey(null));
        assertFalse(resultMap.containsKey(999999999));
    }

    @Test
    @DisplayName("空入参应返回空映射")
    void testResolveBatch_EmptyOrNullKeys_ShouldReturnEmptyMap() {
        assertTrue(warnPlanNameResolver.resolveBatch(Collections.emptySet(), new TranslateContext()).isEmpty());
        assertTrue(warnPlanNameResolver.resolveBatch(null, new TranslateContext()).isEmpty());
    }

    private WarnPlanSaveDto buildWarnPlanSaveDto(String name) {
        return new WarnPlanSaveDto()
                .setName(name)
                .setFirstLevel(new BigDecimal("200"))
                .setSecondLevel(new BigDecimal("100"))
                .setAutoClose(Boolean.TRUE)
                .setRemark("resolver-test");
    }
}
