package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
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

    @Override
    public String command() {
        return AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DATA_UPLOAD);
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
        boolean startsWithSection = payload[0] == Acrel4gPayloadConstants.SECTION_START
                && payload[1] == Acrel4gPayloadConstants.SECTION_START;
        if (startsWithSection) {
            offset = 0;
        } else {
            if (payload.length < 31) {
                log.error("数据上报格式不正确：小于包含序列号的最小长度");
                return null;
            }
            msg.setSerialNumber(Acrel4gParseSupport.readString(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH));
            offset = Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH;
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
        int sectionStartIdx = findPair(payload, idx, Acrel4gPayloadConstants.SECTION_START, Acrel4gPayloadConstants.SECTION_START);
        if (sectionStartIdx < 0) {
            log.error("数据上报格式不正确：没有找到数据段开始的标志");
            return -1;
        }
        int sectionEndIdx = findPair(payload, sectionStartIdx + 2, Acrel4gPayloadConstants.SECTION_END, Acrel4gPayloadConstants.SECTION_END);
        if (sectionEndIdx < 0) {
            log.error("数据上报格式不正确：没有找到数据段结束的标志");
            return -1;
        }

        int contentStart = sectionStartIdx + 2;
        int modbusStartIdx = findPair(payload, contentStart, Acrel4gPayloadConstants.MODBUS_START, Acrel4gPayloadConstants.MODBUS_START);
        if (modbusStartIdx < 0 || modbusStartIdx >= sectionEndIdx) {
            log.error("数据上报格式不正确：没有找到正确的Modbus数据流开始标志");
            return -1;
        }
        int modbusEndIdx = findPair(payload, modbusStartIdx + 2, Acrel4gPayloadConstants.MODBUS_END, Acrel4gPayloadConstants.MODBUS_END);
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
        if (byteCount == Acrel4gPayloadConstants.DATA_LENGTH_RATE4) {
            fillRate4(msg, data);
        } else if (byteCount == Acrel4gPayloadConstants.DATA_LENGTH_RATE8) {
            fillRate8(msg, data);
        }
    }

    /**
     * 解析 4 费率数据区。
     */
    private void fillRate4(DataUploadMessage msg, byte[] data) {
        msg.setTotalEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_TOTAL_OFFSET));
        msg.setHigherEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_HIGHER_OFFSET));
        msg.setHighEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_HIGH_OFFSET));
        msg.setLowEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_LOW_OFFSET));
        msg.setLowerEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_LOWER_OFFSET));
        msg.setDeepLowEnergy(0);
        msg.setTime(Acrel4gParseSupport.parseDateTime(data, Acrel4gPayloadConstants.RATE4_TIME_OFFSET, true));
    }

    /**
     * 解析 8 费率数据区。
     */
    private void fillRate8(DataUploadMessage msg, byte[] data) {
        msg.setTotalEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_TOTAL_OFFSET));
        msg.setHigherEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_HIGHER_OFFSET));
        msg.setHighEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_HIGH_OFFSET));
        msg.setLowEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_LOW_OFFSET));
        msg.setLowerEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_LOWER_OFFSET));
        msg.setDeepLowEnergy(Acrel4gParseSupport.readUInt32(data, Acrel4gPayloadConstants.RATE_DEEP_LOWER_OFFSET));
        msg.setTime(Acrel4gParseSupport.parseDateTime(data, Acrel4gPayloadConstants.RATE8_TIME_OFFSET, false));
    }
}
