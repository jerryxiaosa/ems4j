package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.service.GatewayService;
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
 * 网关名称解析器
 */
@Component
@RequiredArgsConstructor
public class GatewayNameResolver implements BatchLabelResolver<Integer> {

    private final GatewayService gatewayService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }

        Set<Integer> idSet = keys.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        GatewayQueryDto queryDto = new GatewayQueryDto().setIds(idSet.stream().toList());
        return gatewayService.findList(queryDto).stream()
                .filter(gatewayBo -> gatewayBo.getId() != null)
                .collect(Collectors.toMap(GatewayBo::getId, GatewayBo::getGatewayName, (left, right) -> left));
    }
}
