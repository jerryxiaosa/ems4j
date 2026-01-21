package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;
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
}
