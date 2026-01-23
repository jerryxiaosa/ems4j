package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 网关压缩数据上报处理器（委托 DataPacketHandler）。
 */
@Component
@RequiredArgsConstructor
public class DataZipPacketHandler implements GatewayPacketHandler {

    private final DataPacketHandler delegate;

    @Override
    public String command() {
        return GatewayPacketCode.commandKey(GatewayPacketCode.DATA_ZIP);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        delegate.handle(context, message);
    }
}
