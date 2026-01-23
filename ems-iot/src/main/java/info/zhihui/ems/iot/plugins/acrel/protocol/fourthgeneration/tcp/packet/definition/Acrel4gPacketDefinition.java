package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.packet.PacketDefinition;

/**
 * 4G 命令定义：绑定解析器与处理器。
 */
public interface Acrel4gPacketDefinition extends PacketDefinition<AcrelMessage> {

    /**
     * 解析消息体。
     *
     * @param context 设备消息上下文
     * @param payload 消息体
     * @return 解析后的消息
     */
    @Override
    AcrelMessage parse(ProtocolMessageContext context, byte[] payload);
}
