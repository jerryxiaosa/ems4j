package info.zhihui.ems.iot.simulator.config;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorPropertiesTest {

    @Test
    void testBindSimulatorProperties_FromApplicationExampleYaml_ExpectedSuccess() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> propertySources = loader.load("application-example", new ClassPathResource("application-example.yml"));
        Binder binder = new Binder(ConfigurationPropertySources.from(propertySources));

        SimulatorProperties properties = binder.bind("simulator", Bindable.of(SimulatorProperties.class))
                .orElseThrow(() -> new IllegalStateException("绑定 simulator 配置失败"));

        assertEquals("127.0.0.1", properties.getTarget().getHost());
        assertEquals(19500, properties.getTarget().getPort());
        assertEquals("./.data/iot-simulator-state.json", properties.getRuntime().getPersistenceFile());
        assertEquals(60, properties.getRuntime().getHeartbeatIntervalSeconds());
        assertTrue(properties.getReplay().getEnabled());
        assertEquals("DEV001", properties.getDevices().get(0).getDeviceNo());
        assertEquals(VendorEnum.ACREL, properties.getDevices().get(0).getVendor());
        assertEquals("ACREL_4G_DIRECT", properties.getDevices().get(0).getProductCode());
        assertEquals(DeviceAccessModeEnum.DIRECT, properties.getDevices().get(0).getAccessMode());
        assertEquals("001", properties.getDevices().get(0).getMeterAddress());
        assertEquals(ProfileTypeEnum.OFFICE, properties.getDevices().get(0).getProfileType());
    }
}
