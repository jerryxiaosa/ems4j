package info.zhihui.ems.iot.simulator.config;

import lombok.Data;

/**
 * 运行时通用配置。
 */
@Data
public class SimulatorRuntimeProperties {

    /**
     * 心跳间隔，秒。
     */
    private int heartbeatIntervalSeconds = 60;

    /**
     * 本地状态文件路径。
     */
    private String persistenceFile = "./.data/iot-simulator-state.json";
}
