package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.service.WarnPlanService;
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
 * 预警方案名称解析器
 */
@Component
@RequiredArgsConstructor
public class WarnPlanNameResolver implements BatchLabelResolver<Integer> {

    private final WarnPlanService warnPlanService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        Set<Integer> idSet = keys.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        WarnPlanQueryDto queryDto = new WarnPlanQueryDto().setIds(idSet.stream().toList());
        return warnPlanService.findList(queryDto).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(WarnPlanBo::getId, WarnPlanBo::getName, (left, right) -> left));
    }
}
