package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 下发应答命令解析器（0x90）。
 */
@Slf4j
@Component
public class DownlinkPacketParser implements Acrel4gPacketParser {

    private static final int SERIAL_LENGTH = Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH;
    private static final int MIN_RTU_LENGTH = 5;

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DOWNLINK);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null || payload.length < MIN_RTU_LENGTH) {
            log.warn("4G下发应答解析失败，reason=payload长度小于最小RTU长度 payload={} raw={}",
                    safeHex(payload), safeHex(context == null ? null : context.getRawPayload()));
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

        // 只返回了序列号
        if (payload.length == SERIAL_LENGTH && isLikelySerial(payload)) {
            msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, SERIAL_LENGTH));
            msg.setModbusFrame(payload);
            return msg;
        }

        if (isValidRtu(payload)) {
            msg.setModbusFrame(payload);
            return msg;
        }
        log.warn("4G下发应答解析失败，reason=未找到有效的ModbusRTU payload={} raw={}",
                safeHex(payload), safeHex(context == null ? null : context.getRawPayload()));
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

    private String safeHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return HexUtil.bytesToHexString(bytes);
    }
}
