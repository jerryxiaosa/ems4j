package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.components.translate.engine.TranslateContext;
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
class ElectricPricePlanNameResolverIntegrationTest {

    @Autowired
    private ElectricPricePlanNameResolver electricPricePlanNameResolver;

    @Autowired
    private ElectricPricePlanService electricPricePlanService;

    @Test
    @DisplayName("批量解析应返回计费方案名称映射")
    void testResolveBatch_ValidKeys_ShouldReturnNameMap() {
        String planName = "price-plan-" + UUID.randomUUID();
        Integer planId = electricPricePlanService.add(buildElectricPricePlanSaveDto(planName));

        Set<Integer> keySet = new LinkedHashSet<>();
        keySet.add(planId);
        keySet.add(null);
        keySet.add(999999999);

        Map<Integer, String> resultMap = electricPricePlanNameResolver.resolveBatch(keySet, new TranslateContext());

        assertEquals(planName, resultMap.get(planId));
        assertFalse(resultMap.containsKey(null));
        assertFalse(resultMap.containsKey(999999999));
    }

    @Test
    @DisplayName("空入参应返回空映射")
    void testResolveBatch_EmptyOrNullKeys_ShouldReturnEmptyMap() {
        assertTrue(electricPricePlanNameResolver.resolveBatch(Collections.emptySet(), new TranslateContext()).isEmpty());
        assertTrue(electricPricePlanNameResolver.resolveBatch(null, new TranslateContext()).isEmpty());
    }

    private ElectricPricePlanSaveDto buildElectricPricePlanSaveDto(String name) {
        return new ElectricPricePlanSaveDto()
                .setName(name)
                .setPriceHigher(new BigDecimal("1.20"))
                .setPriceHigh(new BigDecimal("1.10"))
                .setPriceLow(new BigDecimal("1.00"))
                .setPriceLower(new BigDecimal("0.90"))
                .setPriceDeepLow(new BigDecimal("0.80"))
                .setIsStep(Boolean.FALSE)
                .setIsCustomPrice(Boolean.TRUE);
    }
}
