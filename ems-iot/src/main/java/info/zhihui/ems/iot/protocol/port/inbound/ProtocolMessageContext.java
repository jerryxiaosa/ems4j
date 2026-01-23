package info.zhihui.ems.iot.protocol.port.inbound;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;

import java.time.LocalDateTime;

/**
 * 协议上行消息上下文，屏蔽传输层细节。
 */
public interface ProtocolMessageContext {

    /**
     * 获取原始数据。
     */
    byte[] getRawPayload();

    /**
     * 获取接收时间。
     */
    LocalDateTime getReceivedAt();

    /**
     * 获取传输协议类型。
     */
    TransportProtocolEnum getTransportType();

    /**
     * 获取会话。
     */
    ProtocolSession getSession();

    /**
     * 获取绑定设备编号。
     */
    String getDeviceNo();

    /**
     * 绑定设备编号。
     */
    void setDeviceNo(String deviceNo);
}
