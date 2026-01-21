package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class DeviceModuleConfigServiceImplIntegrationTest {

    @Autowired
    private DeviceModuleConfigService deviceModuleConfigService;

    @Test
    @DisplayName("getDeviceConfigByModule - 读取区域模块配置")
    void testGetDeviceConfigByModule() {
        DeviceModuleConfigBo config = deviceModuleConfigService.getDeviceConfigByModule(EnergyService.class, 1);
        assertThat(config).isNotNull();
        assertThat(config.getModuleServiceName()).isEqualTo("EnergyService");
        assertThat(config.getImplName()).isEqualTo("AcrelService");
        assertThat(config.getConfigValue()).contains("protocol").contains("TCP");
    }

    @Test
    @DisplayName("getDeviceConfigValue - 解析模块配置为结构体")
    void testGetDeviceConfigValueAsMap() {
        Map<String, Object> value = deviceModuleConfigService.getDeviceConfigValue(EnergyService.class, Map.class, 1);
        assertThat(value).isNotNull();
        assertThat(value.get("protocol")).isEqualTo("TCP");
        assertThat(value.get("timeout")).isEqualTo(5000);
    }

    @Test
    @DisplayName("setDeviceConfigByArea - 更新区域模块实现与配置")
    void testSetDeviceConfigByAreaAndReadBack() {
        // 构造新的配置（将实现切换为MockEnergyService）
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("protocol", "HTTP");
        newValue.put("timeout", 3000);
        String newValueJson = JacksonUtil.toJson(newValue);

        DeviceModuleConfigBo newConfig = new DeviceModuleConfigBo()
                .setModuleServiceName(EnergyService.class.getSimpleName())
                .setImplName("MockEnergyService")
                .setConfigValue(newValueJson);

        deviceModuleConfigService.setDeviceConfigByArea(java.util.List.of(newConfig), 1);

        // 重新读取校验已更新
        DeviceModuleConfigBo updated = deviceModuleConfigService.getDeviceConfigByModule(EnergyService.class, 1);
        assertThat(updated.getImplName()).isEqualTo("MockEnergyService");
        assertThat(updated.getConfigValue()).contains("protocol");

        Map<String, Object> parsed = deviceModuleConfigService.getDeviceConfigValue(EnergyService.class, Map.class, 1);
        assertThat(parsed.get("protocol")).isEqualTo("HTTP");
        assertThat(parsed.get("timeout")).isEqualTo(3000);
    }
}