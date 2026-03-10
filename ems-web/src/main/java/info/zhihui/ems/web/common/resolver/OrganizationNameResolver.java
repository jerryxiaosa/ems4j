package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织名称解析器。
 */
@Component
@RequiredArgsConstructor
public class OrganizationNameResolver implements BatchLabelResolver<Integer> {

    private final OrganizationService organizationService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keySet, TranslateContext context) {
        if (CollectionUtils.isEmpty(keySet)) {
            return Collections.emptyMap();
        }

        Set<Integer> idSet = keySet.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        OrganizationQueryDto queryDto = new OrganizationQueryDto().setIds(idSet);
        return organizationService.findOrganizationList(queryDto).stream()
                .filter(organizationBo -> organizationBo.getId() != null)
                .collect(Collectors.toMap(OrganizationBo::getId, OrganizationBo::getName, (left, right) -> left));
    }
}
