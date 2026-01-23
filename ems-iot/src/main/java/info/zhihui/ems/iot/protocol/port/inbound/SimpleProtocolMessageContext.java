package info.zhihui.ems.iot.protocol.port.inbound;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SimpleProtocolMessageContext implements ProtocolMessageContext {
    private byte[] rawPayload;
    private ProtocolSession session;
    private LocalDateTime receivedAt;
    private TransportProtocolEnum transportType;

    @Override
    public String getDeviceNo() {
        if (session == null) {
            return null;
        }
        return session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO);
    }

    @Override
    public void setDeviceNo(String deviceNo) {
        if (session == null) {
            return;
        }
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, deviceNo);
    }
}
