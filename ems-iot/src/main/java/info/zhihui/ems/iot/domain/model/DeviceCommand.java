package info.zhihui.ems.iot.domain.model;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceCommand {
    private Device device;
    private DeviceCommandTypeEnum type;
    private DeviceCommandRequest payload;
    private ProtocolSignature signature;
}
