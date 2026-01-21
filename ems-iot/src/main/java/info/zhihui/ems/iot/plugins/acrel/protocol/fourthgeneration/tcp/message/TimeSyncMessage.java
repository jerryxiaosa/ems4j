package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class TimeSyncMessage implements AcrelMessage {
    private String serialNumber;
}
