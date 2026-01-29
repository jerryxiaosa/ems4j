package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp;

import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition.Acrel4gPacketDefinition;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AbstractAcrelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 4G TCP 上行报文处理入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Acrel4gTcpInboundHandler extends AbstractAcrelInboundHandler {

    private final Acrel4gPacketRegistry commandRegistry;
    private final Acrel4gFrameCodec frameCodec;

    @Override
    protected FrameDecodeResult decode(byte[] raw) {
        return frameCodec.decode(raw);
    }

    @Override
    protected Acrel4gPacketDefinition resolveDefinition(String commandKey) {
        return commandRegistry.resolve(commandKey);
    }

    @Override
    protected String protocolName() {
        return "4G";
    }

}
