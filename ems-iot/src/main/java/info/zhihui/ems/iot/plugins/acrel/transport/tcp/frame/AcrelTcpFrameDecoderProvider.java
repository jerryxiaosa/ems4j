package info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AcrelTcpFrameDecoderProvider implements NettyFrameDecoderProvider {

    private static final int MAX_FRAME_LENGTH = 2048;

    @Override
    public boolean supports(ProtocolSignature signature) {
        return signature != null && AcrelProtocolConstants.VENDOR.equalsIgnoreCase(signature.getVendor());
    }

    @Override
    public List<ChannelHandler> createDecoders(ProtocolSignature signature) {
        if (signature != null && signature.getAccessMode() == DeviceAccessModeEnum.GATEWAY) {
            return List.of(new AcrelGatewayFrameDecoder());
        }
        return List.of(new AcrelDelimitedFrameDecoder(MAX_FRAME_LENGTH));
    }
}
