package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.domain.model.Device;

/**
 * 设备会话注册入口，供协议处理链路使用。
 */
public interface DeviceSessionRegistry {

    /**
     * 注册设备会话。
     *
     * @param device 设备信息
     * @param session 协议会话
     */
    void register(Device device, ProtocolSession session);
}
