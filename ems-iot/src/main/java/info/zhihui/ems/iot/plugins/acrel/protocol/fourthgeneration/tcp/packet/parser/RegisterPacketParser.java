package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import org.springframework.stereotype.Component;

/**
 * 注册命令解析器（0x84）。
 */
@Component
public class RegisterPacketParser implements Acrel4gPacketParser {

    private static final int REGISTER_BODY_LENGTH = 58;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length != REGISTER_BODY_LENGTH) {
            return null;
        }
        RegisterMessage msg = new RegisterMessage();
        msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, 20));
        msg.setIccid(Acrel4gParseSupport.readString(payload, 20, 30));
        msg.setRssi(Byte.toUnsignedInt(payload[50]));
        msg.setFirmware1(toBcdString(payload[51]) + toBcdString(payload[52]));
        msg.setFirmware2(toBcdString(payload[53]) + toBcdString(payload[54]));
        msg.setFirmware3(toBcdString(payload[55]) + toBcdString(payload[56]));
        msg.setReportIntervalMinutes(Byte.toUnsignedInt(payload[57]));
        return msg;
    }

    /**
     * 将 1 个字节按 BCD（Binary-Coded Decimal）拆成两位数字字符串。
     * 高 4 位表示十位，低 4 位表示个位；本方法未做 BCD 合法性校验（不检查每个半字节是否 <= 9）。
     * 示例： 0x15 -> "15"
     */
    private String toBcdString(byte value) {
        int v = value & 0xFF;
        int high = (v >> 4) & 0x0F;
        int low = v & 0x0F;
        return "" + high + low;
    }
}
