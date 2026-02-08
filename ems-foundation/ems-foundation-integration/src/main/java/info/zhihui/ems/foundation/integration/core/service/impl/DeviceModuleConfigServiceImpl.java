package info.zhihui.ems.foundation.integration.core.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleAreaConfigBo;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;
import info.zhihui.ems.foundation.integration.core.service.CommonDeviceModule;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleConfigService;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.constant.SystemConfigConstant;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceModuleConfigServiceImpl implements DeviceModuleConfigService {

    private final ConfigService configService;

    private List<DeviceModuleConfigBo> getDeviceConfigListByAreaId(Integer areaId) {
        String configString = getAllDeviceConfigString();
        Map<Integer, DeviceModuleAreaConfigBo> configMap = buildParkConfigMap(configString);

        DeviceModuleAreaConfigBo deviceModuleAreaConfigBo = configMap.get(areaId);
        if (deviceModuleAreaConfigBo == null || deviceModuleAreaConfigBo.getDeviceConfigList() == null) {
            log.error("区域配置信息异常，区域id:{}；异常配置信息：{}", areaId, deviceModuleAreaConfigBo);
            throw new NotFoundException("该区域下没有配置信息");
        }

        return deviceModuleAreaConfigBo.getDeviceConfigList();
    }

    private Map<Integer, DeviceModuleAreaConfigBo> buildParkConfigMap(String configString) {
        List<DeviceModuleAreaConfigBo> deviceModuleAreaConfigBoList = JacksonUtil.fromJson(configString, new TypeReference<>() {
        });

        return deviceModuleAreaConfigBoList.stream().collect(Collectors.toMap(DeviceModuleAreaConfigBo::getAreaId, Function.identity(),
                (existing, replacement) -> {
                    if (!existing.equals(replacement)) {
                        log.warn("区域配置重复，areaId={}，使用后者覆盖", replacement.getAreaId());
                    }
                    return replacement;
                }));
    }


    @Override
    public <T extends CommonDeviceModule> DeviceModuleConfigBo getDeviceConfigByModule(Class<T> interfaceType, Integer areaId) {
        String name = interfaceType.getSimpleName();

        List<DeviceModuleConfigBo> equipmentConfigBoList = getDeviceConfigListByAreaId(areaId);
        return equipmentConfigBoList.stream().filter(c -> name.equals(c.getModuleServiceName()))
                .findFirst().orElseThrow(() -> new NotFoundException("尚未配置对应的模块"));
    }

    @Override
    public <T extends CommonDeviceModule, E> E getDeviceConfigValue(Class<T> interfaceType, Class<E> returnObject, Integer areaId) {
        DeviceModuleConfigBo deviceConfigByModule = getDeviceConfigByModule(interfaceType, areaId);
        // e.g. configValue形如：
        // [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"EnergyService","implName":"defaultEnergyServiceImpl","configValue":{"addressUrl":"http://127.0.0.1:8899"}}]}]
        String configValue = deviceConfigByModule.getConfigValue();
        try {
            return JacksonUtil.fromJson(configValue, returnObject);
        } catch (Exception e) {
            log.error("设备配置解析异常[大类型：{}, 配置参数:{}]: {}", interfaceType.getSimpleName(), returnObject.getSimpleName(), configValue);
            throw new BusinessRuntimeException("设备配置解析异常");
        }
    }

    @Override
    public void setDeviceConfigByArea(List<DeviceModuleConfigBo> deviceModuleConfigBoList, Integer areaId) {
        DeviceModuleAreaConfigBo deviceModuleAreaConfigBo = new DeviceModuleAreaConfigBo()
                .setAreaId(areaId)
                .setDeviceConfigList(deviceModuleConfigBoList);

        Map<Integer, DeviceModuleAreaConfigBo> configMap = new HashMap<>();
        try {
            String configString = getAllDeviceConfigString();
            configMap = buildParkConfigMap(configString);
        } catch (NotFoundException ignore) {
        }
        configMap.put(areaId, deviceModuleAreaConfigBo);
        List<DeviceModuleAreaConfigBo> res = new ArrayList<>(configMap.values());

        ConfigUpdateDto updateBo = new ConfigUpdateDto()
                .setConfigKey(SystemConfigConstant.DEVICE_CONFIG)
                .setConfigValue(JacksonUtil.toJson(res));
        configService.update(updateBo);
    }

    private String getAllDeviceConfigString() {
        ConfigBo bo = configService.getByKey(SystemConfigConstant.DEVICE_CONFIG);
        String configString = bo.getConfigValue();

        if (StringUtils.isBlank(configString)) {
            throw new NotFoundException("设备配置信息不存在");
        }
        return configString;
    }
}
