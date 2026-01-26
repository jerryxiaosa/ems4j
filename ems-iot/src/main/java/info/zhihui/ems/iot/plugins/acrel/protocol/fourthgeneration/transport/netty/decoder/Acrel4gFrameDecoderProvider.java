package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.transport.netty.decoder;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Acrel4gFrameDecoderProvider implements NettyFrameDecoderProvider {

    private static final int MAX_FRAME_LENGTH = 2048;

    @Override
    public ProtocolSignature detectTcp(byte[] payload) {
        if (payload == null || payload.length < 2) {
            return null;
        }
        if (payload[0] != AcrelProtocolConstants.DELIMITER || payload[1] != AcrelProtocolConstants.DELIMITER) {
            return null;
        }
        return new ProtocolSignature()
                .setVendor(AcrelProtocolConstants.VENDOR)
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);
    }

    @Override
    public List<ChannelHandler> createDecoders(ProtocolSignature signature) {
        return List.of(new AcrelDelimitedFrameDecoder(MAX_FRAME_LENGTH));
    }
}
