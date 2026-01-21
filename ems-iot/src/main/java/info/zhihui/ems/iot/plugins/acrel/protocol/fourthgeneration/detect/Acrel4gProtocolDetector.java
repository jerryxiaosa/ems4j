package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.detect;

import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import org.springframework.stereotype.Component;

@Component
public class Acrel4gProtocolDetector implements NettyProtocolDetector {

    @Override
    public ProtocolSignature detectTcp(byte[] payload) {
        if (payload == null || payload.length < 2) {
            return null;
        }
        if (payload[0] != AcrelProtocolConstants.DELIMITER || payload[1] != AcrelProtocolConstants.DELIMITER) {
            return null;
        }
        return signature();
    }

    private ProtocolSignature signature() {
        return new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);
    }
}
