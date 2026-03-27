package info.zhihui.ems.iot.simulator.config;

import lombok.Data;

/**
 * IoT 目标服务连接配置。
 */
@Data
public class SimulatorTargetProperties {

    /**
     * IoT 服务主机。
     */
    private String host = "127.0.0.1";

    /**
     * IoT Netty 端口。
     */
    private int port = 19500;

    /**
     * 连接超时时间，毫秒。
     */
    private int connectTimeoutMs = 5000;

    /**
     * 断线重连间隔，毫秒。
     */
    private int reconnectIntervalMs = 3000;
}
