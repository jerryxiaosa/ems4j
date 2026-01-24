package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class DownlinkPacketParserTest {

    private final Acrel4gFrameCodec codec = new Acrel4gFrameCodec();

    @Test
    void command_shouldReturnDownlink() {
        DownlinkPacketParser parser = new DownlinkPacketParser();

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK), parser.command());
    }

    @Test
    void parse_withSerialAndValidRtu_shouldReturnAck() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] modbusFrame = withCrc(new byte[]{0x01, 0x03, 0x02, 0x00, 0x01});

        byte[] payload = new byte[Acrel4gParseSupport.SERIAL_NUMBER_LENGTH + modbusFrame.length];
        byte[] serialBytes = "02121031700227".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        System.arraycopy(modbusFrame, 0, payload, Acrel4gParseSupport.SERIAL_NUMBER_LENGTH, modbusFrame.length);

        AcrelMessage msg = parseMessage(payload, parser, Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(DownlinkAckMessage.class, msg);
        DownlinkAckMessage ack = (DownlinkAckMessage) msg;
        Assertions.assertEquals("02121031700227", ack.getSerialNumber());
        Assertions.assertArrayEquals(modbusFrame, ack.getModbusFrame());
    }

    @Test
    void parse_withValidRtuOnly_shouldReturnAck() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] modbusFrame = withCrc(new byte[]{0x01, 0x03, 0x02, 0x00, 0x02});

        AcrelMessage msg = parseMessage(modbusFrame, parser, Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(DownlinkAckMessage.class, msg);
        DownlinkAckMessage ack = (DownlinkAckMessage) msg;
        Assertions.assertNull(ack.getSerialNumber());
        Assertions.assertArrayEquals(modbusFrame, ack.getModbusFrame());
    }

    @Test
    void parse_withInvalidRtu_shouldReturnNull() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] modbusFrame = withCrc(new byte[]{0x01, 0x03, 0x02, 0x00, 0x03});
        modbusFrame[modbusFrame.length - 1] ^= 0x01;

        AcrelMessage msg = parseMessage(modbusFrame, parser, Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK));

        Assertions.assertNull(msg);
    }

    @Test
    void parse_withShortPayload_shouldReturnNull() {
        DownlinkPacketParser parser = new DownlinkPacketParser();

        Assertions.assertNull(parser.parse(newContext(), new byte[]{0x01, 0x02}));
    }

    @Test
    void parse_withInvalidSerialButValidRtuPayload_shouldFallbackToRtuOnly() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] body = new byte[Acrel4gParseSupport.SERIAL_NUMBER_LENGTH + 3];
        body[0] = 0x01;
        body[1] = (byte) 0xFF;
        body[20] = 0x01;
        body[21] = 0x02;
        body[22] = 0x03;
        byte[] payload = withCrc(body);

        AcrelMessage msg = parser.parse(newContext(), payload);

        Assertions.assertNotNull(msg);
        DownlinkAckMessage ack = (DownlinkAckMessage) msg;
        Assertions.assertNull(ack.getSerialNumber());
        Assertions.assertArrayEquals(payload, ack.getModbusFrame());
    }

    @Test
    void parse_withSerialButInvalidRtu_shouldReturnNull() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] modbusFrame = withCrc(new byte[]{0x01, 0x03, 0x02, 0x00, 0x05});
        modbusFrame[modbusFrame.length - 1] ^= 0x01;

        byte[] payload = new byte[Acrel4gParseSupport.SERIAL_NUMBER_LENGTH + modbusFrame.length];
        byte[] serialBytes = "02121031700227".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);
        System.arraycopy(modbusFrame, 0, payload, Acrel4gParseSupport.SERIAL_NUMBER_LENGTH, modbusFrame.length);

        AcrelMessage msg = parser.parse(newContext(), payload);

        Assertions.assertNull(msg);
    }

    @Test
    void parse_withShortRtuShouldReturnNull() {
        DownlinkPacketParser parser = new DownlinkPacketParser();
        byte[] payload = new byte[]{0x01, 0x02, 0x03, 0x04};

        Assertions.assertNull(parser.parse(newContext(), payload));
    }

    private AcrelMessage parseMessage(byte[] payload, Acrel4gPacketParser parser, String expectedCommandKey) {
        byte[] frame = codec.encode(Acrel4gPacketCode.DOWNLINK, payload);
        Object parsed = parseFrame(frame);
        Assertions.assertEquals(expectedCommandKey, extractCommand(parsed));
        return parser.parse(newContext(), extractPayload(parsed));
    }

    private SimpleProtocolMessageContext newContext() {
        return new SimpleProtocolMessageContext();
    }

    private Object parseFrame(byte[] frame) {
        return codec.decode(frame);
    }

    private String extractCommand(Object parsed) {
        return ((FrameDecodeResult) parsed).commandKey();
    }

    private byte[] extractPayload(Object parsed) {
        return ((FrameDecodeResult) parsed).payload();
    }

    private byte[] withCrc(byte[] body) {
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }
}
