package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

class DataUploadPacketParserTest {

    private final Acrel4gFrameCodec codec = new Acrel4gFrameCodec();

    @Test
    void testCommand_ShouldReturnDataUpload() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD), parser.command());
    }

    @Test
    void testParse_NullPayload_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        Assertions.assertNull(parser.parse(newContext(), null));
    }

    @Test
    void testParse_DataUploadWithoutSerial_ShouldParseMeterAddressAndEnergy() {
        LocalDateTime time = LocalDateTime.of(2021, 10, 21, 19, 37, 1);
        byte[] modbusFrame = buildRate4ModbusFrame(100, 10, 20, 30, 40, time);
        byte[] payload = buildDataUploadPayloadWithoutSerial("1-1", modbusFrame);
        byte[] frame = codec.encode(Acrel4gPacketCode.DATA_UPLOAD, payload);

        AcrelMessage msg = parseMessage(frame, new DataUploadPacketParser(),
                Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(DataUploadMessage.class, msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertNull(upload.getSerialNumber());
        Assertions.assertEquals("1-1", upload.getMeterAddress());
        Assertions.assertEquals(100, upload.getTotalEnergy());
        Assertions.assertEquals(10, upload.getHigherEnergy());
        Assertions.assertEquals(20, upload.getHighEnergy());
        Assertions.assertEquals(30, upload.getLowEnergy());
        Assertions.assertEquals(40, upload.getLowerEnergy());
        Assertions.assertEquals(0, upload.getDeepLowEnergy());
        Assertions.assertEquals(time, upload.getTime());
    }

    @Test
    void testParse_DataUploadWithSerial_ShouldParseSerialMeterAddressAndEnergy() {
        LocalDateTime time = LocalDateTime.of(2021, 10, 21, 19, 37, 1);
        byte[] modbusFrame = buildRate4ModbusFrame(100, 10, 20, 30, 40, time);
        byte[] body = buildDataUploadPayloadWithoutSerial("1-1", modbusFrame);

        byte[] payload = new byte[20 + body.length];
        byte[] serialBytes = "02121031700227".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        System.arraycopy(body, 0, payload, 20, body.length);

        byte[] frame = codec.encode(Acrel4gPacketCode.DATA_UPLOAD, payload);

        AcrelMessage msg = parseMessage(frame, new DataUploadPacketParser(),
                Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(DataUploadMessage.class, msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertEquals("02121031700227", upload.getSerialNumber());
        Assertions.assertEquals("1-1", upload.getMeterAddress());
        Assertions.assertEquals(100, upload.getTotalEnergy());
        Assertions.assertEquals(10, upload.getHigherEnergy());
        Assertions.assertEquals(20, upload.getHighEnergy());
        Assertions.assertEquals(30, upload.getLowEnergy());
        Assertions.assertEquals(40, upload.getLowerEnergy());
        Assertions.assertEquals(0, upload.getDeepLowEnergy());
        Assertions.assertEquals(time, upload.getTime());
    }

    @Test
    void testParse_DataUploadRate8_ShouldParseDeepValleyEnergyAndTime() {
        LocalDateTime time = LocalDateTime.of(2024, 10, 30, 16, 24, 0);
        byte[] modbusFrame = buildRate8ModbusFrame(120, 5, 6, 7, 8, 9, time);
        byte[] payload = buildDataUploadPayloadWithoutSerial("1-1", modbusFrame);
        byte[] frame = codec.encode(Acrel4gPacketCode.DATA_UPLOAD, payload);

        AcrelMessage msg = parseMessage(frame, new DataUploadPacketParser(),
                Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(DataUploadMessage.class, msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertEquals(120, upload.getTotalEnergy());
        Assertions.assertEquals(5, upload.getHigherEnergy());
        Assertions.assertEquals(6, upload.getHighEnergy());
        Assertions.assertEquals(7, upload.getLowEnergy());
        Assertions.assertEquals(8, upload.getLowerEnergy());
        Assertions.assertEquals(9, upload.getDeepLowEnergy());
        Assertions.assertEquals(time, upload.getTime());
    }

    @Test
    void testParse_DataUploadTooShort_ShouldReturnNull() {
        byte[] frame = codec.encode(Acrel4gPacketCode.DATA_UPLOAD, new byte[]{1, 2, 3, 4, 5});
        Object parsed = parseFrame(frame);
        AcrelMessage msg = new DataUploadPacketParser().parse(newContext(), extractPayload(parsed));
        Assertions.assertNull(msg);
    }

    @Test
    void testParse_NoSectionAndPayloadShorterThanSerial_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[20];
        payload[0] = 0x41;
        payload[1] = 0x41;

        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    @Test
    void testParse_SerialWithoutSection_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[31];
        byte[] serial = "02121031700227".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serial, 0, payload, 0, serial.length);
        for (int i = 20; i < payload.length; i++) {
            payload[i] = 0x41;
        }
        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    @Test
    void testParse_SectionMissingEnd_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[]{0x5b, 0x5b, '1', '-', '1', 0x28, 0x28, 0x01, 0x03, 0x00, 0x00};
        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    @Test
    void testParse_SectionMissingModbusStart_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[]{0x5b, 0x5b, '1', '-', '1', 'A', 'B', 'C', 'D', 0x5d, 0x5d};
        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    @Test
    void testParse_SectionMissingModbusEnd_ShouldReturnNull() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[]{0x5b, 0x5b, '1', '-', '1', 0x28, 0x28, 0x01, 0x03, 0x00, 0x5d, 0x5d};
        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    @Test
    void testParse_ModbusFrameTooShort_ShouldReturnMessageWithDefaults() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] payload = new byte[]{0x5b, 0x5b, '1', '-', '1', 0x28, 0x28, 0x29, 0x29, 0x5d, 0x5d};
        AcrelMessage msg = parser.parse(newContext(), payload);
        Assertions.assertNotNull(msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertEquals("1-1", upload.getMeterAddress());
        Assertions.assertEquals(0, upload.getTotalEnergy());
        Assertions.assertNull(upload.getTime());
    }

    @Test
    void testParse_ModbusFrameByteCountTooLarge_ShouldReturnMessageWithDefaults() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] modbusFrame = new byte[]{0x01, 0x03, 0x0A, 0x00, 0x00};
        byte[] payload = buildDataUploadPayloadWithoutSerial("1-1", modbusFrame);
        AcrelMessage msg = parser.parse(newContext(), payload);
        Assertions.assertNotNull(msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertEquals(0, upload.getTotalEnergy());
        Assertions.assertNull(upload.getTime());
    }

    @Test
    void testParse_ModbusFrameUnknownByteCount_ShouldReturnMessageWithDefaults() {
        DataUploadPacketParser parser = new DataUploadPacketParser();
        byte[] modbusFrame = new byte[]{0x01, 0x03, 0x01, 0x7F, 0x00};
        byte[] payload = buildDataUploadPayloadWithoutSerial("1-1", modbusFrame);
        AcrelMessage msg = parser.parse(newContext(), payload);
        Assertions.assertNotNull(msg);
        DataUploadMessage upload = (DataUploadMessage) msg;
        Assertions.assertEquals(0, upload.getTotalEnergy());
        Assertions.assertNull(upload.getTime());
    }

    private byte[] buildDataUploadPayloadWithoutSerial(String meterAddress, byte[] modbusFrame) {
        byte[] addressBytes = meterAddress.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[2 + addressBytes.length + 2 + modbusFrame.length + 2 + 2];
        int idx = 0;
        payload[idx++] = 0x5b;
        payload[idx++] = 0x5b;
        System.arraycopy(addressBytes, 0, payload, idx, addressBytes.length);
        idx += addressBytes.length;
        payload[idx++] = 0x28;
        payload[idx++] = 0x28;
        System.arraycopy(modbusFrame, 0, payload, idx, modbusFrame.length);
        idx += modbusFrame.length;
        payload[idx++] = 0x29;
        payload[idx++] = 0x29;
        payload[idx++] = 0x5d;
        payload[idx] = 0x5d;
        return payload;
    }

    private byte[] buildRate4ModbusFrame(int total, int higher, int high, int low, int lower, LocalDateTime time) {
        byte[] data = new byte[0x60];
        putUInt32(data, 42, total);
        putUInt32(data, 46, higher);
        putUInt32(data, 50, high);
        putUInt32(data, 54, low);
        putUInt32(data, 58, lower);
        fillRate4Time(data, time);
        return wrapModbusFrame((byte) 0x60, data);
    }

    private byte[] buildRate8ModbusFrame(int total, int higher, int high, int low, int lower, int deepLow, LocalDateTime time) {
        byte[] data = new byte[0x90];
        putUInt32(data, 42, total);
        putUInt32(data, 46, higher);
        putUInt32(data, 50, high);
        putUInt32(data, 54, low);
        putUInt32(data, 58, lower);
        putUInt32(data, 96, deepLow);
        fillRate8Time(data, time);
        return wrapModbusFrame((byte) 0x90, data);
    }

    private byte[] wrapModbusFrame(byte byteCount, byte[] data) {
        byte[] modbus = new byte[3 + data.length + 2];
        modbus[0] = 0x01;
        modbus[1] = 0x03;
        modbus[2] = byteCount;
        System.arraycopy(data, 0, modbus, 3, data.length);
        return modbus;
    }

    private void putUInt32(byte[] data, int offset, int value) {
        data[offset] = (byte) ((value >> 24) & 0xFF);
        data[offset + 1] = (byte) ((value >> 16) & 0xFF);
        data[offset + 2] = (byte) ((value >> 8) & 0xFF);
        data[offset + 3] = (byte) (value & 0xFF);
    }

    private void fillRate4Time(byte[] data, LocalDateTime time) {
        data[80] = (byte) (time.getYear() - 2000);
        data[81] = (byte) time.getMonthValue();
        data[82] = (byte) time.getDayOfMonth();
        data[83] = (byte) (time.getDayOfWeek().getValue() % 7);
        data[84] = (byte) time.getHour();
        data[85] = (byte) time.getMinute();
        data[86] = (byte) time.getSecond();
    }

    private void fillRate8Time(byte[] data, LocalDateTime time) {
        data[80] = (byte) (time.getYear() - 2000);
        data[81] = (byte) time.getMonthValue();
        data[82] = (byte) time.getDayOfMonth();
        data[83] = (byte) time.getHour();
        data[84] = (byte) time.getMinute();
        data[85] = (byte) time.getSecond();
    }

    private Object parseFrame(byte[] frame) {
        Object parsed = parseFrameResult(frame);
        Assertions.assertNotNull(parsed);
        return parsed;
    }

    private AcrelMessage parseMessage(byte[] frame, Acrel4gPacketParser parser, String expectedCommandKey) {
        Object parsed = parseFrame(frame);
        Assertions.assertEquals(expectedCommandKey, extractCommand(parsed));
        return parser.parse(newContext(), extractPayload(parsed));
    }

    private SimpleProtocolMessageContext newContext() {
        return new SimpleProtocolMessageContext();
    }

    private FrameDecodeResult parseFrameResult(byte[] frame) {
        return codec.decode(frame);
    }

    private String extractCommand(Object parsed) {
        return ((FrameDecodeResult) parsed).commandKey();
    }

    private byte[] extractPayload(Object parsed) {
        return ((FrameDecodeResult) parsed).payload();
    }
}
