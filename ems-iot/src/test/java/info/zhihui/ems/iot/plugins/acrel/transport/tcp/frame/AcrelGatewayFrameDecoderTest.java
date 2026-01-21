package info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGatewayFrameDecoderTest {

    @Test
    void decode_whenFrameComplete_shouldOutputFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelGatewayFrameDecoder());
        byte[] frame = new byte[]{
                0x1f, 0x1f, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x11, 0x22, 0x33
        };

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void decode_whenFragmented_shouldWaitForCompleteFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelGatewayFrameDecoder());
        byte[] first = new byte[]{0x1f, 0x1f, 0x01, 0x00, 0x00};
        byte[] second = new byte[]{0x00, 0x03, 0x11, 0x22, 0x33};

        channel.writeInbound(Unpooled.wrappedBuffer(first));
        Assertions.assertNull(channel.readInbound());

        channel.writeInbound(Unpooled.wrappedBuffer(second));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(new byte[]{
                0x1f, 0x1f, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x11, 0x22, 0x33
        }, decoded);
    }

    @Test
    void decode_whenHeadMismatch_shouldSkipAndFindNext() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelGatewayFrameDecoder());
        byte[] input = new byte[]{
                0x00,
                0x1f, 0x1f, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x7f
        };

        channel.writeInbound(Unpooled.wrappedBuffer(input));

        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(new byte[]{
                0x1f, 0x1f, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x7f
        }, decoded);
    }
}
