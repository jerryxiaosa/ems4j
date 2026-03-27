package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 安科瑞 4G 模拟器报文工厂。
 */
@Component
@RequiredArgsConstructor
public class Acrel4gMessageFactory {

    private final Acrel4gFrameCodec frameCodec;

    /**
     * 构造设备注册上行帧。
     */
    public byte[] buildRegisterFrame(RegisterMessage registerMessage) {
        Objects.requireNonNull(registerMessage, "registerMessage cannot be null");
        byte[] payload = new byte[Acrel4gPayloadConstants.REGISTER_BODY_LENGTH];
        writeFixedString(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH, registerMessage.getSerialNumber());
        writeFixedString(payload, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH, Acrel4gPayloadConstants.ICCID_LENGTH, registerMessage.getIccid());
        payload[Acrel4gPayloadConstants.REGISTER_RSSI_OFFSET] = (byte) (registerMessage.getRssi() & 0xFF);
        writeBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE1_OFFSET, registerMessage.getFirmware1());
        writeBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE2_OFFSET, registerMessage.getFirmware2());
        writeBcdVersion(payload, Acrel4gPayloadConstants.REGISTER_FIRMWARE3_OFFSET, registerMessage.getFirmware3());
        payload[Acrel4gPayloadConstants.REGISTER_REPORT_INTERVAL_OFFSET] =
                (byte) (registerMessage.getReportIntervalMinutes() & 0xFF);
        return frameCodec.encode(Acrel4gCommandConstants.REGISTER, payload);
    }

    /**
     * 构造设备心跳上行帧。
     */
    public byte[] buildHeartbeatFrame(HeartbeatMessage heartbeatMessage) {
        Objects.requireNonNull(heartbeatMessage, "heartbeatMessage cannot be null");
        return frameCodec.encode(Acrel4gCommandConstants.HEARTBEAT, null);
    }

    /**
     * 构造电量上报上行帧。
     */
    public byte[] buildDataUploadFrame(DataUploadMessage dataUploadMessage) {
        Objects.requireNonNull(dataUploadMessage, "dataUploadMessage cannot be null");
        byte[] body = buildDataUploadBody(dataUploadMessage);
        return frameCodec.encode(Acrel4gCommandConstants.DATA_UPLOAD, body);
    }

    /**
     * 构造数据上报 body，序列号存在时放在最前面，后面跟业务 section。
     */
    private byte[] buildDataUploadBody(DataUploadMessage dataUploadMessage) {
        byte[] serialBytes = buildFixedString(Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH, dataUploadMessage.getSerialNumber());
        byte[] sectionBytes = buildSectionBytes(dataUploadMessage);
        if (StringUtils.isBlank(dataUploadMessage.getSerialNumber())) {
            return sectionBytes;
        }
        byte[] payload = new byte[serialBytes.length + sectionBytes.length];
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        System.arraycopy(sectionBytes, 0, payload, serialBytes.length, sectionBytes.length);
        return payload;
    }

    /**
     * 构造单个 section 数据块，格式为 [[meterAddress((modbusFrame))]]。
     */
    private byte[] buildSectionBytes(DataUploadMessage dataUploadMessage) {
        byte[] meterAddressBytes = valueOrEmpty(dataUploadMessage.getMeterAddress()).getBytes(StandardCharsets.UTF_8);
        byte[] modbusFrame = buildModbusFrame(dataUploadMessage);
        byte[] payload = new byte[2 + meterAddressBytes.length + 2 + modbusFrame.length + 2 + 2];
        int index = 0;
        payload[index++] = Acrel4gPayloadConstants.SECTION_START;
        payload[index++] = Acrel4gPayloadConstants.SECTION_START;
        System.arraycopy(meterAddressBytes, 0, payload, index, meterAddressBytes.length);
        index += meterAddressBytes.length;
        payload[index++] = Acrel4gPayloadConstants.MODBUS_START;
        payload[index++] = Acrel4gPayloadConstants.MODBUS_START;
        System.arraycopy(modbusFrame, 0, payload, index, modbusFrame.length);
        index += modbusFrame.length;
        payload[index++] = Acrel4gPayloadConstants.MODBUS_END;
        payload[index++] = Acrel4gPayloadConstants.MODBUS_END;
        payload[index++] = Acrel4gPayloadConstants.SECTION_END;
        payload[index] = Acrel4gPayloadConstants.SECTION_END;
        return payload;
    }

    /**
     * 构造 4G 表主动上报使用的 Modbus 读响应体。
     */
    private byte[] buildModbusFrame(DataUploadMessage dataUploadMessage) {
        boolean rate8 = dataUploadMessage.getDeepLowEnergy() > 0;
        byte byteCount = (byte) (rate8 ? Acrel4gPayloadConstants.DATA_LENGTH_RATE8 : Acrel4gPayloadConstants.DATA_LENGTH_RATE4);
        byte[] data = new byte[rate8 ? Acrel4gPayloadConstants.DATA_LENGTH_RATE8 : Acrel4gPayloadConstants.DATA_LENGTH_RATE4];
        putUInt32(data, Acrel4gPayloadConstants.RATE_TOTAL_OFFSET, dataUploadMessage.getTotalEnergy());
        putUInt32(data, Acrel4gPayloadConstants.RATE_HIGHER_OFFSET, dataUploadMessage.getHigherEnergy());
        putUInt32(data, Acrel4gPayloadConstants.RATE_HIGH_OFFSET, dataUploadMessage.getHighEnergy());
        putUInt32(data, Acrel4gPayloadConstants.RATE_LOW_OFFSET, dataUploadMessage.getLowEnergy());
        putUInt32(data, Acrel4gPayloadConstants.RATE_LOWER_OFFSET, dataUploadMessage.getLowerEnergy());
        if (rate8) {
            putUInt32(data, Acrel4gPayloadConstants.RATE_DEEP_LOWER_OFFSET, dataUploadMessage.getDeepLowEnergy());
            fillRate8Time(data, dataUploadMessage.getTime());
        } else {
            fillRate4Time(data, dataUploadMessage.getTime());
        }

        byte[] modbusFrame = new byte[3 + data.length];
        modbusFrame[0] = 0x01;
        modbusFrame[1] = (byte) ModbusRtuBuilder.FUNCTION_READ;
        modbusFrame[2] = byteCount;
        System.arraycopy(data, 0, modbusFrame, 3, data.length);
        return modbusFrame;
    }

    /**
     * 按四费率协议布局写入上报时间。
     */
    private void fillRate4Time(byte[] data, LocalDateTime time) {
        LocalDateTime reportTime = defaultTime(time);
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET] = (byte) (reportTime.getYear() - 2000);
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 1] = (byte) reportTime.getMonthValue();
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 2] = (byte) reportTime.getDayOfMonth();
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 3] = (byte) (reportTime.getDayOfWeek().getValue() % 7);
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 4] = (byte) reportTime.getHour();
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 5] = (byte) reportTime.getMinute();
        data[Acrel4gPayloadConstants.RATE4_TIME_OFFSET + 6] = (byte) reportTime.getSecond();
    }

    /**
     * 按八费率协议布局写入上报时间。
     */
    private void fillRate8Time(byte[] data, LocalDateTime time) {
        LocalDateTime reportTime = defaultTime(time);
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET] = (byte) (reportTime.getYear() - 2000);
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET + 1] = (byte) reportTime.getMonthValue();
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET + 2] = (byte) reportTime.getDayOfMonth();
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET + 3] = (byte) reportTime.getHour();
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET + 4] = (byte) reportTime.getMinute();
        data[Acrel4gPayloadConstants.RATE8_TIME_OFFSET + 5] = (byte) reportTime.getSecond();
    }

    /**
     * 兜底上报时间，未指定时取当前时间。
     */
    private LocalDateTime defaultTime(LocalDateTime time) {
        return time == null ? LocalDateTime.now() : time;
    }

    /**
     * 将形如 0102 的版本号写入两个 BCD 字节。
     */
    private void writeBcdVersion(byte[] payload, int offset, String version) {
        String normalizedVersion = normalizeVersion(version);
        payload[offset] = toBcdByte(normalizedVersion.substring(0, 2));
        payload[offset + 1] = toBcdByte(normalizedVersion.substring(2, 4));
    }

    /**
     * 将两位十进制字符转换为一个 BCD 字节。
     */
    private byte toBcdByte(String twoDigits) {
        int high = Character.digit(twoDigits.charAt(0), 10);
        int low = Character.digit(twoDigits.charAt(1), 10);
        return (byte) ((high << 4) | low);
    }

    /**
     * 版本号不足四位时左侧补零，超过四位时截断。
     */
    private String normalizeVersion(String version) {
        String value = valueOrEmpty(version);
        if (value.length() >= 4) {
            return value.substring(0, 4);
        }
        return "0".repeat(4 - value.length()) + value;
    }

    /**
     * 按大端顺序写入 32 位无符号整数。
     */
    private void putUInt32(byte[] data, int offset, int value) {
        data[offset] = (byte) ((value >> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
    }

    /**
     * 将定长字符串写入目标数组，超长截断，不足补零。
     */
    private void writeFixedString(byte[] target, int offset, int length, String value) {
        byte[] valueBytes = buildFixedString(length, value);
        System.arraycopy(valueBytes, 0, target, offset, valueBytes.length);
    }

    /**
     * 构造定长字符串字节数组，超长截断，不足补零。
     */
    private byte[] buildFixedString(int length, String value) {
        byte[] result = new byte[length];
        byte[] source = valueOrEmpty(value).getBytes(StandardCharsets.UTF_8);
        int copyLength = Math.min(source.length, length);
        System.arraycopy(source, 0, result, 0, copyLength);
        return result;
    }

    /**
     * 规避空字符串字段的空指针处理。
     */
    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
