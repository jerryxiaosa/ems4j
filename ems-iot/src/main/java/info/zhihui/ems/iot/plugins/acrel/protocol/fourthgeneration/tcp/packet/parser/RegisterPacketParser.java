package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import org.springframework.stereotype.Component;

/**
 * 注册命令解析器（0x84）。
 */
@Component
public class RegisterPacketParser implements Acrel4gPacketParser {

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.REGISTER);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length != Acrel4gPayloadConstants.REGISTER_BODY_LENGTH) {
            return null;
        }
        RegisterMessage msg = new RegisterMessage();
        msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH));
        msg.setIccid(Acrel4gParseSupport.readString(payload,
                Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH,
                Acrel4gPayloadConstants.ICCID_LENGTH));
        msg.setRssi(Byte.toUnsignedInt(payload[Acrel4gPayloadConstants.REGISTER_RSSI_OFFSET]));
        msg.setFirmware1(readBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE1_OFFSET));
        msg.setFirmware2(readBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE2_OFFSET));
        msg.setFirmware3(readBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE3_OFFSET));
        msg.setReportIntervalMinutes(Byte.toUnsignedInt(payload[Acrel4gPayloadConstants.REGISTER_REPORT_INTERVAL_OFFSET]));
        return msg;
    }

    private String readBcdVersion(byte[] payload, int offset) {
        return toBcdString(payload[offset]) + toBcdString(payload[offset + 1]);
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
