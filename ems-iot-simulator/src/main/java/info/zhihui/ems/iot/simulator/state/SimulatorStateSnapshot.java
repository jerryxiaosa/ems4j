package info.zhihui.ems.iot.simulator.state;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟器状态快照。
 */
@Data
@Accessors(chain = true)
public class SimulatorStateSnapshot {

    private Map<String, DeviceRuntimeState> deviceStateMap = new ConcurrentHashMap<>();
}
