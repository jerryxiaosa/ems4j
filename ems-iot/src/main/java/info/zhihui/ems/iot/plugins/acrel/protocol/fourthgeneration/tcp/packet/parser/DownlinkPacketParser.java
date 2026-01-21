package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 下发应答命令解析器（0x90）。
 */
@Component
public class DownlinkPacketParser implements Acrel4gPacketParser {

    private static final int SERIAL_LENGTH = Acrel4gParseSupport.SERIAL_NUMBER_LENGTH;
    private static final int MIN_RTU_LENGTH = 5;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length < MIN_RTU_LENGTH) {
            return null;
        }
        DownlinkAckMessage msg = new DownlinkAckMessage();

        if (payload.length >= SERIAL_LENGTH + MIN_RTU_LENGTH) {
            byte[] serialBytes = Arrays.copyOfRange(payload, 0, SERIAL_LENGTH);
            byte[] modbusFrame = Arrays.copyOfRange(payload, SERIAL_LENGTH, payload.length);
            if (isLikelySerial(serialBytes) && isValidRtu(modbusFrame)) {
                msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, SERIAL_LENGTH));
                msg.setModbusFrame(modbusFrame);
                return msg;
            }
        }

        if (isValidRtu(payload)) {
            msg.setModbusFrame(payload);
            return msg;
        }
        return null;
    }

    private boolean isLikelySerial(byte[] serialBytes) {
        if (serialBytes == null || serialBytes.length == 0) {
            return false;
        }
        boolean hasValue = false;
        for (byte b : serialBytes) {
            if (b == 0x00) {
                continue;
            }
            if (b < 0x20 || b > 0x7E) {
                return false;
            }
            hasValue = true;
        }
        return hasValue;
    }

    private boolean isValidRtu(byte[] frame) {
        if (frame == null || frame.length < MIN_RTU_LENGTH) {
            return false;
        }
        int dataLen = frame.length - 2;
        byte[] data = Arrays.copyOf(frame, dataLen);
        int expected = ModbusCrcUtil.crcInt(data);
        int low = frame[frame.length - 2] & 0xFF;
        int high = frame[frame.length - 1] & 0xFF;
        int actual = (high << 8) | low;
        return expected == actual;
    }
}
