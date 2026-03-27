package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayHeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.constant.AcrelGatewayCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import org.springframework.stereotype.Component;

/**
 * 网关心跳报文解析器。
 */
@Component("gatewayHeartbeatPacketParser")
public class HeartbeatPacketParser implements GatewayPacketParser {

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(AcrelGatewayCommandConstants.HEARTBEAT);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        return new GatewayHeartbeatMessage();
    }
}
