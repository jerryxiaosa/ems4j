package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.detect;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import org.springframework.stereotype.Component;

@Component
public class AcrelGatewayProtocolDetector implements NettyProtocolDetector {

    @Override
    public ProtocolSignature detectTcp(byte[] payload) {
        if (payload == null || payload.length < 2) {
            return null;
        }
        short head = toShort(payload[0], payload[1]);
        if (head != AcrelProtocolConstants.GATEWAY_HEAD) {
            return null;
        }
        return new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);
    }

    private short toShort(byte high, byte low) {
        return (short) (((high & 0xff) << 8) | (low & 0xff));
    }
}
