package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorProperties;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import info.zhihui.ems.iot.simulator.state.SimulatorStateSnapshot;
import info.zhihui.ems.iot.simulator.state.StateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模拟器上下文装载入口。
 */
@Service
@RequiredArgsConstructor
public class SimulatorLauncher {

    private static final String SWITCH_ON = "ON";

    private final SimulatorProperties simulatorProperties;
    private final StateStore stateStore;

    /**
     * 从持久化存储加载设备运行快照；如果文件不存在则返回空快照。
     */
    public SimulatorStateSnapshot loadStateSnapshot() {
        SimulatorStateSnapshot stateSnapshot = stateStore.load();
        return stateSnapshot == null ? new SimulatorStateSnapshot() : stateSnapshot;
    }

    /**
     * 根据配置设备列表和历史快照，组装本次启动所需的设备上下文集合。
     */
    public List<SimulatorDeviceContext> loadDeviceContexts(SimulatorStateSnapshot stateSnapshot) {
        SimulatorStateSnapshot currentStateSnapshot = stateSnapshot == null ? new SimulatorStateSnapshot() : stateSnapshot;
        Map<String, DeviceRuntimeState> deviceStateMap = currentStateSnapshot.getDeviceStateMap();
        List<SimulatorDeviceContext> deviceContexts = new ArrayList<>();
        for (SimulatorDeviceProperties deviceProperties : simulatorProperties.getDevices()) {
            DeviceRuntimeState runtimeState = initializeRuntimeState(deviceProperties, deviceStateMap.get(deviceProperties.getDeviceNo()));
            deviceContexts.add(new SimulatorDeviceContext()
                    .setDeviceProperties(deviceProperties)
                    .setRuntimeState(runtimeState));
            deviceStateMap.put(deviceProperties.getDeviceNo(), runtimeState);
        }
        return deviceContexts;
    }

    /**
     * 初始化单台设备的运行态，优先复用历史状态并补齐本次启动必需字段。
     */
    private DeviceRuntimeState initializeRuntimeState(SimulatorDeviceProperties deviceProperties,
                                                      DeviceRuntimeState existingRuntimeState) {
        DeviceRuntimeState runtimeState = existingRuntimeState == null ? new DeviceRuntimeState() : existingRuntimeState;
        runtimeState.setDeviceNo(deviceProperties.getDeviceNo())
                .setVendor(deviceProperties.getVendor())
                .setProductCode(deviceProperties.getProductCode())
                .setAccessMode(deviceProperties.getAccessMode());
        if (isBlank(runtimeState.getSwitchStatus())) {
            runtimeState.setSwitchStatus(SWITCH_ON);
        }
        if (runtimeState.getReplayCompleted() == null) {
            runtimeState.setReplayCompleted(!Boolean.TRUE.equals(simulatorProperties.getReplay().getEnabled()));
        }
        return runtimeState;
    }

    /**
     * 判断字符串是否为空白，用于初始化缺失的开关状态。
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
