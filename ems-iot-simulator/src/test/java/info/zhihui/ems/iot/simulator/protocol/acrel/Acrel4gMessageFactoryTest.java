package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gPayloadConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

class Acrel4gMessageFactoryTest {

    private static final int RATE4_MODBUS_LENGTH = 3 + Acrel4gPayloadConstants.DATA_LENGTH_RATE4;
    private static final int RATE8_MODBUS_LENGTH = 3 + Acrel4gPayloadConstants.DATA_LENGTH_RATE8;

    private final Acrel4gFrameCodec frameCodec = new Acrel4gFrameCodec();
    private final Acrel4gMessageFactory messageFactory = new Acrel4gMessageFactory(frameCodec);

    @Test
    void buildRegisterFrame_shouldEncodeRegisterPayload() {
        RegisterMessage registerMessage = new RegisterMessage()
                .setSerialNumber("02121031700227")
                .setIccid("898604510919C0452888")
                .setRssi(27)
                .setFirmware1("0002")
                .setFirmware2("0100")
                .setFirmware3("0100")
                .setReportIntervalMinutes(30);

        FrameDecodeResult decodeResult = frameCodec.decode(messageFactory.buildRegisterFrame(registerMessage));

        Assertions.assertNull(decodeResult.reason());
        Assertions.assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.REGISTER), decodeResult.commandKey());
        Assertions.assertEquals(Acrel4gPayloadConstants.REGISTER_BODY_LENGTH, decodeResult.payload().length);
        Assertions.assertEquals("02121031700227",
                readAscii(decodeResult.payload(), 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH).trim());
        Assertions.assertEquals(27, decodeResult.payload()[Acrel4gPayloadConstants.REGISTER_RSSI_OFFSET] & 0xFF);
        Assertions.assertEquals(30,
                decodeResult.payload()[Acrel4gPayloadConstants.REGISTER_REPORT_INTERVAL_OFFSET] & 0xFF);
    }

    @Test
    void buildHeartbeatFrame_shouldEncodeHeartbeatCommandWithoutPayload() {
        FrameDecodeResult decodeResult = frameCodec.decode(messageFactory.buildHeartbeatFrame(new HeartbeatMessage()));

        Assertions.assertNull(decodeResult.reason());
        Assertions.assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.HEARTBEAT), decodeResult.commandKey());
        Assertions.assertEquals(0, decodeResult.payload().length);
    }

    @Test
    void buildDataUploadFrame_shouldEncodeRate4Payload() {
        DataUploadMessage message = new DataUploadMessage()
                .setSerialNumber("02121031700227")
                .setMeterAddress("00000001")
                .setTime(LocalDateTime.of(2026, 2, 1, 17, 0, 8))
                .setTotalEnergy(100)
                .setHigherEnergy(10)
                .setHighEnergy(20)
                .setLowEnergy(30)
                .setLowerEnergy(40)
                .setDeepLowEnergy(0);

        FrameDecodeResult decodeResult = frameCodec.decode(messageFactory.buildDataUploadFrame(message));
        byte[] payload = decodeResult.payload();
        byte[] modbusFrame = extractModbusFrame(payload);

        Assertions.assertNull(decodeResult.reason());
        Assertions.assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DATA_UPLOAD), decodeResult.commandKey());
        Assertions.assertEquals("02121031700227",
                readAscii(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH).trim());
        Assertions.assertTrue(readAscii(payload, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH, 12).contains("00000001"));
        Assertions.assertEquals(RATE4_MODBUS_LENGTH, modbusFrame.length);
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, modbusFrame[1] & 0xFF);
        Assertions.assertEquals(Acrel4gPayloadConstants.DATA_LENGTH_RATE4, modbusFrame[2] & 0xFF);
    }

    @Test
    void buildDataUploadFrame_whenDeepLowEnergyExists_shouldEncodeRate8Payload() {
        DataUploadMessage message = new DataUploadMessage()
                .setSerialNumber("02121031700227")
                .setMeterAddress("00000009")
                .setTime(LocalDateTime.of(2026, 2, 18, 2, 33, 18))
                .setTotalEnergy(120)
                .setHigherEnergy(5)
                .setHighEnergy(6)
                .setLowEnergy(7)
                .setLowerEnergy(8)
                .setDeepLowEnergy(9);

        FrameDecodeResult decodeResult = frameCodec.decode(messageFactory.buildDataUploadFrame(message));
        byte[] payload = decodeResult.payload();
        byte[] modbusFrame = extractModbusFrame(payload);

        Assertions.assertNull(decodeResult.reason());
        Assertions.assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.DATA_UPLOAD), decodeResult.commandKey());
        Assertions.assertEquals("02121031700227",
                readAscii(payload, 0, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH).trim());
        Assertions.assertTrue(readAscii(payload, Acrel4gPayloadConstants.SERIAL_NUMBER_LENGTH, 12).contains("00000009"));
        Assertions.assertEquals(RATE8_MODBUS_LENGTH, modbusFrame.length);
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, modbusFrame[1] & 0xFF);
        Assertions.assertEquals(Acrel4gPayloadConstants.DATA_LENGTH_RATE8, modbusFrame[2] & 0xFF);
    }

    private byte[] extractModbusFrame(byte[] payload) {
        int sectionStart = indexOf(payload, new byte[]{Acrel4gPayloadConstants.MODBUS_START, Acrel4gPayloadConstants.MODBUS_START});
        int sectionEnd = indexOf(payload, new byte[]{Acrel4gPayloadConstants.MODBUS_END, Acrel4gPayloadConstants.MODBUS_END});
        return Arrays.copyOfRange(payload, sectionStart + 2, sectionEnd);
    }

    private int indexOf(byte[] source, byte[] marker) {
        for (int index = 0; index <= source.length - marker.length; index++) {
            boolean matched = true;
            for (int offset = 0; offset < marker.length; offset++) {
                if (source[index + offset] != marker[offset]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return index;
            }
        }
        throw new IllegalArgumentException("marker not found");
    }

    private String readAscii(byte[] payload, int offset, int length) {
        return new String(payload, offset, length, StandardCharsets.UTF_8);
    }
}
