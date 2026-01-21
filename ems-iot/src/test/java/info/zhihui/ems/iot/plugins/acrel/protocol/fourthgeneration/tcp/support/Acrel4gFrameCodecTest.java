package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support;

import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.util.HexUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

class Acrel4gFrameCodecTest {

    @Test
    void testEncode_NullBody_ShouldBuildFrameWithEmptyPayload() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        byte command = Acrel4gPacketCode.HEARTBEAT;
        byte[] frame = codec.encode(command, null);

        Assertions.assertEquals(7, frame.length);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[0]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[1]);
        Assertions.assertEquals(command, frame[2]);
        byte[] expectedCrc = ModbusCrcUtil.crc(new byte[]{command});
        Assertions.assertEquals(expectedCrc[0], frame[3]);
        Assertions.assertEquals(expectedCrc[1], frame[4]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[5]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[6]);
    }

    @Test
    void testEncode_WithPayload_ShouldPlacePayloadAndCrc() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        byte command = Acrel4gPacketCode.DOWNLINK;
        byte[] payload = new byte[]{0x01, 0x02, 0x03};
        byte[] frame = codec.encode(command, payload);

        Assertions.assertEquals(10, frame.length);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[0]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[1]);
        Assertions.assertEquals(command, frame[2]);
        Assertions.assertArrayEquals(payload, Arrays.copyOfRange(frame, 3, 6));
        byte[] crcInput = new byte[1 + payload.length];
        crcInput[0] = command;
        System.arraycopy(payload, 0, crcInput, 1, payload.length);
        byte[] expectedCrc = ModbusCrcUtil.crc(crcInput);
        Assertions.assertEquals(expectedCrc[0], frame[6]);
        Assertions.assertEquals(expectedCrc[1], frame[7]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[8]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[9]);
    }

    @Test
    void testEncodeAck_ShouldMatchEncodeEmptyPayload() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        byte command = Acrel4gPacketCode.REGISTER;
        byte[] expected = codec.encodeAck(command);
        String expectedHex = HexUtil.bytesToHexString(expected);

        Assertions.assertEquals("7B7B84BF237D7D", expectedHex);
    }

    @Test
    void testEncodeTimeSync_ShouldEncodeTimeBody() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        LocalDateTime time = LocalDateTime.of(2021, 10, 21, 19, 36, 42);
        byte[] frame = codec.encode(Acrel4gPacketCode.TIME_SYNC, buildTimeSyncBody(time));

        byte[] expectedBody = new byte[]{0x15, 0x0a, 0x15, 0x04, 0x13, 0x24, 0x2a};
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[0]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER, frame[1]);
        Assertions.assertEquals(Acrel4gPacketCode.TIME_SYNC, frame[2]);
        Assertions.assertArrayEquals(expectedBody, Arrays.copyOfRange(frame, 3, 10));
        byte[] crcInput = new byte[1 + expectedBody.length];
        crcInput[0] = Acrel4gPacketCode.TIME_SYNC;
        System.arraycopy(expectedBody, 0, crcInput, 1, expectedBody.length);
        byte[] expectedCrc = ModbusCrcUtil.crc(crcInput);
        Assertions.assertEquals(expectedCrc[0], frame[10]);
        Assertions.assertEquals(expectedCrc[1], frame[11]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[12]);
        Assertions.assertEquals(AcrelProtocolConstants.DELIMITER_END, frame[13]);
    }

    private byte[] buildTimeSyncBody(LocalDateTime time) {
        byte[] body = new byte[7];
        int year = time.getYear() % 100;
        body[0] = (byte) year;
        body[1] = (byte) time.getMonthValue();
        body[2] = (byte) time.getDayOfMonth();
        int dayOfWeek = time.getDayOfWeek().getValue() % 7;
        body[3] = (byte) dayOfWeek;
        body[4] = (byte) time.getHour();
        body[5] = (byte) time.getMinute();
        body[6] = (byte) time.getSecond();
        return body;
    }
}
