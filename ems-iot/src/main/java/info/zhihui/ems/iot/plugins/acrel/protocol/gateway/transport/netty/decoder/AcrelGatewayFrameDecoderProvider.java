package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.transport.netty.decoder;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constant.AcrelProtocolConstants;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AcrelGatewayFrameDecoderProvider implements NettyFrameDecoderProvider {

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
                .setVendor(VendorEnum.ACREL.name())
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);
    }

    @Override
    public List<ChannelHandler> createDecoders(ProtocolSignature signature) {
        return List.of(new AcrelGatewayFrameDecoder());
    }

    private short toShort(byte high, byte low) {
        return (short) (((high & 0xff) << 8) | (low & 0xff));
    }
}
