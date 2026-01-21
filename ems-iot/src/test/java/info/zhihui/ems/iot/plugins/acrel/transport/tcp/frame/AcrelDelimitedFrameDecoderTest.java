package info.zhihui.ems.iot.plugins.acrel.transport.tcp.frame;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelDelimitedFrameDecoderTest {

    @Test
    void decode_whenFrameComplete_shouldOutputFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(32));
        byte[] frame = new byte[]{0x7b, 0x7b, 0x01, 0x02, 0x02, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void decode_whenPrefixed_shouldSkipToFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(32));
        byte[] frame = new byte[]{0x7b, 0x7b, 0x05, 0x7d, 0x7d};
        byte[] input = new byte[]{0x00, 0x11, 0x22, 0x7b, 0x7b, 0x05, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(input));

        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void decode_whenFragmented_shouldWaitForCompleteFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(32));
        byte[] frame = new byte[]{0x7b, 0x7b, 0x01, 0x02, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x7b, 0x7b, 0x01}));
        Assertions.assertNull(channel.readInbound());

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{0x02, 0x7d, 0x7d}));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void decode_whenFrameTooLong_shouldDrop() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(6));
        byte[] frame = new byte[]{0x7b, 0x7b, 0x01, 0x02, 0x03, 0x04, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(frame));

        Assertions.assertNull(channel.readInbound());
    }

    @Test
    void decode_whenNoStartDelimiter_shouldDiscardBytes() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(32));
        byte[] garbage = new byte[]{0x01, 0x02, 0x03};
        byte[] frame = new byte[]{0x7b, 0x7b, 0x01, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(garbage));
        Assertions.assertNull(channel.readInbound());

        channel.writeInbound(Unpooled.wrappedBuffer(frame));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }

    @Test
    void decode_whenNoEndAndTooLong_shouldDropBuffer() {
        EmbeddedChannel channel = new EmbeddedChannel(new AcrelDelimitedFrameDecoder(5));
        byte[] partial = new byte[]{0x7b, 0x7b, 0x01, 0x02, 0x03, 0x04};
        byte[] frame = new byte[]{0x7b, 0x7b, 0x05, 0x7d, 0x7d};

        channel.writeInbound(Unpooled.wrappedBuffer(partial));
        Assertions.assertNull(channel.readInbound());

        channel.writeInbound(Unpooled.wrappedBuffer(frame));
        byte[] decoded = channel.readInbound();
        Assertions.assertArrayEquals(frame, decoded);
    }
}
