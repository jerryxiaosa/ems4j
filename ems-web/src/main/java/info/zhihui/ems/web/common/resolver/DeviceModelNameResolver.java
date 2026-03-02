package info.zhihui.ems.web.common.resolver;

import info.zhihui.ems.components.translate.engine.TranslateContext;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备型号名称解析器
 */
@Component
@RequiredArgsConstructor
public class DeviceModelNameResolver implements BatchLabelResolver<Integer> {

    private final DeviceModelService deviceModelService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keySet, TranslateContext context) {
        if (CollectionUtils.isEmpty(keySet)) {
            return Collections.emptyMap();
        }

        Set<Integer> idSet = keySet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }

        DeviceModelQueryDto queryDto = new DeviceModelQueryDto().setIds(idSet.stream().toList());
        return deviceModelService.findList(queryDto).stream()
                .filter(deviceModelBo -> deviceModelBo.getId() != null)
                .collect(Collectors.toMap(DeviceModelBo::getId, DeviceModelBo::getModelName, (left, right) -> left));
    }
}
