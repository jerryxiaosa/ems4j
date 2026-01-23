package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.TimeSyncMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import org.springframework.stereotype.Component;

/**
 * 对时命令解析器（0x93）。
 */
@Component
public class TimeSyncPacketParser implements Acrel4gPacketParser {

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length == 0) {
            return null;
        }
        TimeSyncMessage msg = new TimeSyncMessage();
        if (payload.length >= Acrel4gParseSupport.SERIAL_NUMBER_LENGTH) {
            msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, Acrel4gParseSupport.SERIAL_NUMBER_LENGTH));
        }
        return msg;
    }
}
