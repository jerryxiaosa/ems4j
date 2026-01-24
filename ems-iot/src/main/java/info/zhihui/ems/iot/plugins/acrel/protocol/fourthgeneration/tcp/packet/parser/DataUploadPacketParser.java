package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 数据上报命令解析器（0x91）。
 */
@Slf4j
@Component
public class DataUploadPacketParser implements Acrel4gPacketParser {

    private static final int DATA_LENGTH_RATE4 = 0x60;
    private static final int DATA_LENGTH_RATE8 = 0x90;
    private static final int RATE_TOTAL_OFFSET = 42;
    private static final int RATE_HIGHER_OFFSET = 46;
    private static final int RATE_HIGH_OFFSET = 50;
    private static final int RATE_LOW_OFFSET = 54;
    private static final int RATE_LOWER_OFFSET = 58;
    private static final int RATE_DEEP_LOWER_OFFSET = 96;
    private static final int RATE4_TIME_OFFSET = 80;
    private static final int RATE8_TIME_OFFSET = 80;
    private static final byte SECTION_START = 0x5b; // '['，采集段以 "[[" 开头
    private static final byte SECTION_END = 0x5d; // ']'，采集段以 "]]" 结束
    private static final byte MODBUS_START = 0x28; // '('，Modbus 数据以 "((" 开始
    private static final byte MODBUS_END = 0x29; // ')'，Modbus 数据以 "))" 结束

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD);
    }

    @Override
    public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
        if (payload == null) {
            return null;
        }
        DataUploadMessage msg = new DataUploadMessage();

        if (payload.length < 11) {
            // [[仪表地址(())]]
            log.error("数据上报格式不正确：小于包含固定字段的最小长度");
            return null;
        }

        int offset;
        boolean startsWithSection = payload[0] == SECTION_START && payload[1] == SECTION_START;
        if (startsWithSection) {
            offset = 0;
        } else {
            if (payload.length < 31) {
                log.error("数据上报格式不正确：小于包含序列号的最小长度");
                return null;
            }
            msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, Acrel4gParseSupport.SERIAL_NUMBER_LENGTH));
            offset = Acrel4gParseSupport.SERIAL_NUMBER_LENGTH;
        }
        int res = fillSegment(msg, payload, offset);
        if (res == -1) {
            return null;
        }

        return msg;
    }

    /**
     * 提取数据上报报文的段采集内容（仪表地址与 Modbus 数据流）。
     */
    private int fillSegment(DataUploadMessage msg, byte[] payload, int offset) {
        int idx = Math.max(0, offset);
        int sectionStartIdx = findPair(payload, idx, SECTION_START, SECTION_START);
        if (sectionStartIdx < 0) {
            log.error("数据上报格式不正确：没有找到数据段开始的标志");
            return -1;
        }
        int sectionEndIdx = findPair(payload, sectionStartIdx + 2, SECTION_END, SECTION_END);
        if (sectionEndIdx < 0) {
            log.error("数据上报格式不正确：没有找到数据段结束的标志");
            return -1;
        }

        int contentStart = sectionStartIdx + 2;
        int modbusStartIdx = findPair(payload, contentStart, MODBUS_START, MODBUS_START);
        if (modbusStartIdx < 0 || modbusStartIdx >= sectionEndIdx) {
            log.error("数据上报格式不正确：没有找到正确的Modbus数据流开始标志");
            return -1;
        }
        int modbusEndIdx = findPair(payload, modbusStartIdx + 2, MODBUS_END, MODBUS_END);
        if (modbusEndIdx < 0 || modbusEndIdx >= sectionEndIdx) {
            log.error("数据上报格式不正确：没有找到正确的Modbus数据流结束标志");
            return -1;
        }

        String meterAddress = new String(payload, contentStart, modbusStartIdx - contentStart, StandardCharsets.UTF_8).trim();
        byte[] modbusFrame = Arrays.copyOfRange(payload, modbusStartIdx + 2, modbusEndIdx);
        msg.setMeterAddress(meterAddress);
        parseModbusData(msg, modbusFrame);

        return 0;
    }

    /**
     * 从指定起点查找连续双字节标记的位置。
     */
    private int findPair(byte[] payload, int start, byte first, byte second) {
        for (int i = start; i < payload.length - 1; i++) {
            if (payload[i] == first && payload[i + 1] == second) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析 Modbus 数据区并填充电量与时间字段。
     */
    private void parseModbusData(DataUploadMessage msg, byte[] modbusFrame) {
        if (modbusFrame == null || modbusFrame.length < 5) {
            return;
        }
        int byteCount = Byte.toUnsignedInt(modbusFrame[2]);
        int dataStart = 3;
        int dataEnd = dataStart + byteCount;
        if (modbusFrame.length < dataEnd) {
            return;
        }
        byte[] data = Arrays.copyOfRange(modbusFrame, dataStart, dataEnd);
        if (byteCount == DATA_LENGTH_RATE4) {
            fillRate4(msg, data);
        } else if (byteCount == DATA_LENGTH_RATE8) {
            fillRate8(msg, data);
        }
    }

    /**
     * 解析 4 费率数据区。
     */
    private void fillRate4(DataUploadMessage msg, byte[] data) {
        msg.setTotalEnergy(Acrel4gParseSupport.readUInt32(data, RATE_TOTAL_OFFSET));
        msg.setHigherEnergy(Acrel4gParseSupport.readUInt32(data, RATE_HIGHER_OFFSET));
        msg.setHighEnergy(Acrel4gParseSupport.readUInt32(data, RATE_HIGH_OFFSET));
        msg.setLowEnergy(Acrel4gParseSupport.readUInt32(data, RATE_LOW_OFFSET));
        msg.setLowerEnergy(Acrel4gParseSupport.readUInt32(data, RATE_LOWER_OFFSET));
        msg.setDeepLowEnergy(0);
        msg.setTime(Acrel4gParseSupport.parseDateTime(data, RATE4_TIME_OFFSET, true));
    }

    /**
     * 解析 8 费率数据区。
     */
    private void fillRate8(DataUploadMessage msg, byte[] data) {
        msg.setTotalEnergy(Acrel4gParseSupport.readUInt32(data, RATE_TOTAL_OFFSET));
        msg.setHigherEnergy(Acrel4gParseSupport.readUInt32(data, RATE_HIGHER_OFFSET));
        msg.setHighEnergy(Acrel4gParseSupport.readUInt32(data, RATE_HIGH_OFFSET));
        msg.setLowEnergy(Acrel4gParseSupport.readUInt32(data, RATE_LOW_OFFSET));
        msg.setLowerEnergy(Acrel4gParseSupport.readUInt32(data, RATE_LOWER_OFFSET));
        msg.setDeepLowEnergy(Acrel4gParseSupport.readUInt32(data, RATE_DEEP_LOWER_OFFSET));
        msg.setTime(Acrel4gParseSupport.parseDateTime(data, RATE8_TIME_OFFSET, false));
    }
}
