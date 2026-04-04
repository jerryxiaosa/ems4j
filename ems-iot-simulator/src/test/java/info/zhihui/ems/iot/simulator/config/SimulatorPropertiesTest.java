package info.zhihui.ems.iot.simulator.config;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertNull(properties.getReplay().getStartTime());
        assertNull(properties.getReplay().getEndTime());
        assertEquals("DEV001", properties.getDevices().get(0).getDeviceNo());
        assertEquals(VendorEnum.ACREL, properties.getDevices().get(0).getVendor());
        assertEquals("ACREL_4G_DIRECT", properties.getDevices().get(0).getProductCode());
        assertEquals(DeviceAccessModeEnum.DIRECT, properties.getDevices().get(0).getAccessMode());
        assertEquals("001", properties.getDevices().get(0).getMeterAddress());
        assertEquals(ProfileTypeEnum.OFFICE, properties.getDevices().get(0).getProfileType());
    }

    @Test
    void testBindSimulatorProperties_FromApplicationDockerYaml_ExpectedSuccess() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> propertySources = loader.load("application-docker", new ClassPathResource("application-docker.yml"));
        CompositePropertySource compositePropertySource = new CompositePropertySource("docker");
        propertySources.forEach(compositePropertySource::addPropertySource);

        assertEquals("${SIMULATOR_TARGET_HOST:iot}", compositePropertySource.getProperty("simulator.target.host"));
        assertEquals("${SIMULATOR_TARGET_PORT:19500}", compositePropertySource.getProperty("simulator.target.port"));
        assertEquals("${SIMULATOR_RUNTIME_PERSISTENCE_FILE:/app/.data/iot-simulator-state.json}",
                compositePropertySource.getProperty("simulator.runtime.persistence-file"));
        assertEquals("${SIMULATOR_REPLAY_ENABLED:true}", compositePropertySource.getProperty("simulator.replay.enabled"));
        assertEquals("DEV001", compositePropertySource.getProperty("simulator.devices[0].device-no"));
    }
}
