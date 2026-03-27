package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.TimeSyncMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import org.springframework.stereotype.Component;

/**
 * 对时命令解析器（0x93）。
 */
@Component
public class TimeSyncPacketParser implements Acrel4gPacketParser {

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.TIME_SYNC);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length == 0) {
            return null;
        }
        TimeSyncMessage msg = new TimeSyncMessage();
        if (payload.length >= Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH) {
            msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH));
        }
        return msg;
    }
}
