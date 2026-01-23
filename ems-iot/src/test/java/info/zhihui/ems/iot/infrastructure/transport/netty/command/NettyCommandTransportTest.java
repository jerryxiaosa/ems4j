package info.zhihui.ems.iot.infrastructure.transport.netty.command;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

class NettyCommandTransportTest {

    @Test
    void testSendWithAck_PayloadNull_ThrowsIllegalArgumentException() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyCommandTransport transport = new NettyCommandTransport(channelManager);

        Assertions.assertThrows(IllegalArgumentException.class, () -> transport.sendWithAck("dev-1", null));
        Mockito.verifyNoInteractions(channelManager);
    }

    @Test
    void testSendWithAck_PayloadProvided_DelegatesToChannelManager() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyCommandTransport transport = new NettyCommandTransport(channelManager);
        byte[] payload = new byte[]{1, 2, 3};
        CompletableFuture<byte[]> expected = CompletableFuture.completedFuture(new byte[]{9});
        Mockito.when(channelManager.sendInQueue(Mockito.eq("dev-1"), Mockito.any())).thenReturn(expected);

        CompletableFuture<byte[]> result = transport.sendWithAck("dev-1", payload);

        Assertions.assertSame(expected, result);
        ArgumentCaptor<ByteBuf> bufferCaptor = ArgumentCaptor.forClass(ByteBuf.class);
        Mockito.verify(channelManager).sendInQueue(Mockito.eq("dev-1"), bufferCaptor.capture());
        ByteBuf buffer = bufferCaptor.getValue();
        byte[] actual = new byte[buffer.readableBytes()];
        buffer.getBytes(0, actual);
        Assertions.assertArrayEquals(payload, actual);
        ReferenceCountUtil.release(buffer);
    }

    @Test
    void testCompletePending_DelegatesToChannelManager() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyCommandTransport transport = new NettyCommandTransport(channelManager);
        byte[] payload = new byte[]{1};
        Mockito.when(channelManager.completePending("dev-1", payload)).thenReturn(true);

        boolean result = transport.completePending("dev-1", payload);

        Assertions.assertTrue(result);
        Mockito.verify(channelManager).completePending("dev-1", payload);
    }

    @Test
    void testFailPending_DelegatesToChannelManager() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyCommandTransport transport = new NettyCommandTransport(channelManager);
        RuntimeException failure = new RuntimeException("send failed");

        transport.failPending("dev-1", failure);

        Mockito.verify(channelManager).failPending("dev-1", failure);
    }
}
