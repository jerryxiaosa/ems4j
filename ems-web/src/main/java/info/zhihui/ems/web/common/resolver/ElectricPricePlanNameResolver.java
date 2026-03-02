package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 计费方案名称解析器
 */
@Component
@RequiredArgsConstructor
public class ElectricPricePlanNameResolver implements BatchLabelResolver<Integer> {

    private final ElectricPricePlanService electricPricePlanService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        Set<Integer> idSet = keys.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        ElectricPricePlanQueryDto queryDto = new ElectricPricePlanQueryDto().setIds(idSet.stream().toList());
        return electricPricePlanService.findList(queryDto).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ElectricPricePlanBo::getId, ElectricPricePlanBo::getName, (left, right) -> left));
    }
}
