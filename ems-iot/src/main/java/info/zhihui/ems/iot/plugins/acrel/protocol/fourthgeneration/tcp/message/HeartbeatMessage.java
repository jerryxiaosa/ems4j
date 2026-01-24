package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HeartbeatMessage implements AcrelMessage {
}
