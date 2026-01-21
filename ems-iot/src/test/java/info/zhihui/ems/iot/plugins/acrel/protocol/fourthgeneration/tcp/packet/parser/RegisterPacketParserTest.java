package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class RegisterPacketParserTest {

    private final Acrel4gFrameCodec codec = new Acrel4gFrameCodec();

    @Test
    void command_shouldReturnRegister() {
        RegisterPacketParser parser = new RegisterPacketParser();

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER), parser.command());
    }

    @Test
    void testParse_Register_ShouldParseFields() {
        byte[] payload = new byte[58];

        byte[] serialBytes = "02121031700227".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serialBytes, 0, payload, 0, serialBytes.length);

        byte[] iccidBytes = "898604510919C0452888".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(iccidBytes, 0, payload, 20, iccidBytes.length);

        payload[50] = 0x1b; // RSSI = 27
        payload[51] = 0x00;
        payload[52] = 0x02;
        payload[53] = 0x01;
        payload[54] = 0x00;
        payload[55] = 0x01;
        payload[56] = 0x00;
        payload[57] = 0x1e; // 30min

        byte[] frame = codec.encode(Acrel4gPacketCode.REGISTER, payload);

        AcrelMessage msg = parseMessage(frame, new RegisterPacketParser(),
                Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(RegisterMessage.class, msg);
        RegisterMessage register = (RegisterMessage) msg;
        Assertions.assertEquals("02121031700227", register.getSerialNumber());
        Assertions.assertEquals("898604510919C0452888", register.getIccid());
        Assertions.assertEquals(27, register.getRssi());
        Assertions.assertEquals("0002", register.getFirmware1());
        Assertions.assertEquals("0100", register.getFirmware2());
        Assertions.assertEquals("0100", register.getFirmware3());
        Assertions.assertEquals(30, register.getReportIntervalMinutes());
    }

    @Test
    void testParse_RegisterPayloadLengthMismatch_ShouldReturnNull() {
        byte[] payload = new byte[57];
        byte[] frame = codec.encode(Acrel4gPacketCode.REGISTER, payload);

        Object parsed = parseFrame(frame);
        AcrelMessage msg = new RegisterPacketParser().parse(newContext(), extractPayload(parsed));
        Assertions.assertNull(msg);
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
