package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;

import java.time.LocalDateTime;

/**
 * 协议上行消息上下文，屏蔽传输层细节。
 */
public interface ProtocolMessageContext {

    byte[] getRawPayload();

    LocalDateTime getReceivedAt();

    TransportProtocolEnum getTransportType();

    ProtocolSession getSession();

    /**
     * 获取绑定设备编号，统一从会话属性读取。
     */
    default String getDeviceNo() {
        ProtocolSession session = getSession();
        if (session == null) {
            return null;
        }
        return session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO);
    }

    /**
     * 绑定设备编号，统一写入会话属性。
     */
    default void setDeviceNo(String deviceNo) {
        ProtocolSession session = getSession();
        if (session == null) {
            return;
        }
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, deviceNo);
    }
}
