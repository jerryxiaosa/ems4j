package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.packet.PacketParser;

/**
 * 4G 命令解析器接口。
 */
public interface Acrel4gPacketParser extends PacketParser<AcrelMessage> {

    /**
     * 解析命令消息体（不含起止符、命令字、CRC）。
     *
     * @param context 设备消息上下文
     * @param payload 消息体
     * @return 解析后的协议消息，失败返回 null
     */
    @Override
    AcrelMessage parse(ProtocolMessageContext context, byte[] payload);
}
