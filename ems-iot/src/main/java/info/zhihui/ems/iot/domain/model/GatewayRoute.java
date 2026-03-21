package info.zhihui.ems.iot.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网关链路路由结果。
 */
@Data
@Accessors(chain = true)
public class GatewayRoute {

    /**
     * 实际承载通信的网关设备。
     */
    private Device gateway;

    /**
     * 当前命令或探测真正作用的目标设备。
     */
    private Device targetDevice;

    /**
     * 目标设备是否就是网关自身。
     */
    private boolean gatewaySelf;
}
