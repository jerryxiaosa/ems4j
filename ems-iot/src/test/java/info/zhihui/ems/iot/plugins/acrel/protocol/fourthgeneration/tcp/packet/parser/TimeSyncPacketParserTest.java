package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.parser;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.TimeSyncMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class TimeSyncPacketParserTest {

    private final Acrel4gFrameCodec codec = new Acrel4gFrameCodec();

    @Test
    void command_shouldReturnTimeSync() {
        TimeSyncPacketParser parser = new TimeSyncPacketParser();

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC), parser.command());
    }

    @Test
    void testParse_TimeSyncEmptyPayload_ShouldReturnNull() {
        TimeSyncPacketParser parser = new TimeSyncPacketParser();
        Assertions.assertNull(parser.parse(newContext(), new byte[0]));
    }

    @Test
    void testParse_TimeSyncShortPayload_ShouldReturnMessage() {
        TimeSyncPacketParser parser = new TimeSyncPacketParser();

        AcrelMessage message = parser.parse(newContext(), new byte[]{0x01});

        Assertions.assertNotNull(message);
        Assertions.assertInstanceOf(TimeSyncMessage.class, message);
        TimeSyncMessage timeSync = (TimeSyncMessage) message;
        Assertions.assertNull(timeSync.getSerialNumber());
    }

    @Test
    void testParse_TimeSyncWithSerial_ShouldParseSerialOnly() {
        String serial = "02121031700227";

        byte[] body = new byte[20];
        byte[] serialBytes = serial.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(serialBytes, 0, body, 0, serialBytes.length);

        byte[] frame = codec.encode(Acrel4gPacketCode.TIME_SYNC, body);

        AcrelMessage msg = parseMessage(frame, new TimeSyncPacketParser(),
                Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC));

        Assertions.assertNotNull(msg);
        Assertions.assertInstanceOf(TimeSyncMessage.class, msg);
        TimeSyncMessage timeSync = (TimeSyncMessage) msg;
        Assertions.assertEquals(serial, timeSync.getSerialNumber());
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
