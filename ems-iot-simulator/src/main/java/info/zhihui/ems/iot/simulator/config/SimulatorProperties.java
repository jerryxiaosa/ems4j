package info.zhihui.ems.iot.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟器主配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {

    private SimulatorTargetProperties target = new SimulatorTargetProperties();
    private SimulatorRuntimeProperties runtime = new SimulatorRuntimeProperties();
    private SimulatorReplayProperties replay = new SimulatorReplayProperties();
    private List<SimulatorDeviceProperties> devices = new ArrayList<>();
}
