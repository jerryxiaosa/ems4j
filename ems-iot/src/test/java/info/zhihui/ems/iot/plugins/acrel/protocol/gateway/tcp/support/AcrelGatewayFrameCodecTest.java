package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.decode.ProtocolDecodeErrorEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGatewayFrameCodecTest {

    @Test
    void testDecode_WhenFrameNull_ShouldReturnFrameTooShort() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();

        FrameDecodeResult result = codec.decode(null);

        Assertions.assertEquals(ProtocolDecodeErrorEnum.FRAME_TOO_SHORT, result.reason());
        Assertions.assertEquals(0, result.payload().length);
    }

    @Test
    void testDecode_WhenFrameTooShort_ShouldReturnFrameTooShort() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        byte[] frame = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06};

        FrameDecodeResult result = codec.decode(frame);

        Assertions.assertEquals(ProtocolDecodeErrorEnum.FRAME_TOO_SHORT, result.reason());
    }

    @Test
    void testDecode_WhenFrameValid_ShouldReturnPayload() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        byte[] payload = new byte[]{0x11, 0x22};
        byte[] frame = codec.encode((byte) 0x10, payload);

        FrameDecodeResult result = codec.decode(frame);

        Assertions.assertNull(result.reason());
        Assertions.assertEquals(GatewayPacketCode.commandKey((byte) 0x10), result.commandKey());
        Assertions.assertArrayEquals(payload, result.payload());
    }

    @Test
    void testEncode_WhenPayloadNull_ShouldBuildEmptyFrame() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();

        byte[] frame = codec.encode((byte) 0x01, null);
        FrameDecodeResult result = codec.decode(frame);

        Assertions.assertEquals(7, frame.length);
        Assertions.assertNull(result.reason());
        Assertions.assertEquals(0, result.payload().length);
    }
}
