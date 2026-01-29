package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition.GatewayPacketDefinition;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AbstractAcrelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安科瑞网关 TCP 报文处理器入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AcrelGatewayTcpInboundHandler extends AbstractAcrelInboundHandler {

    private final AcrelGatewayFrameCodec frameCodec;
    private final GatewayPacketRegistry packetRegistry;

    @Override
    protected FrameDecodeResult decode(byte[] raw) {
        return frameCodec.decode(raw);
    }

    @Override
    protected GatewayPacketDefinition resolveDefinition(String commandKey) {
        return packetRegistry.resolve(commandKey);
    }

    @Override
    protected String protocolName() {
        return "网关";
    }

}
