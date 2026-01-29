package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DataUploadMessage implements AcrelMessage {

    private String serialNumber;
    private String meterAddress;
    private LocalDateTime time;
    private int totalEnergy;
    private int higherEnergy;
    private int highEnergy;
    private int lowEnergy;
    private int lowerEnergy;
    private int deepLowEnergy;
}
