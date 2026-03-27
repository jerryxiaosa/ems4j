package info.zhihui.ems.iot.simulator.runtime;

import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 单台模拟电表的最小运行上下文。
 */
@Data
@Accessors(chain = true)
public class SimulatorDeviceContext {

    private SimulatorDeviceProperties deviceProperties;
    private DeviceRuntimeState runtimeState;
    private String connectionId;
    private LocalDateTime connectedAt;
    private LocalDateTime lastCommunicationAt;
}
