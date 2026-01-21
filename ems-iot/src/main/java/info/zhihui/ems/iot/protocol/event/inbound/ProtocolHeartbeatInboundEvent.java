package info.zhihui.ems.iot.protocol.event.inbound;

import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Inbound heartbeat event from protocol layer.
 */
@Data
@Accessors(chain = true)
public class ProtocolHeartbeatInboundEvent implements ProtocolInboundEvent {
    private String deviceNo;
    private String sessionId;
    private LocalDateTime receivedAt;
    private TransportProtocolEnum transportType;
    private String rawPayloadHex;
}
