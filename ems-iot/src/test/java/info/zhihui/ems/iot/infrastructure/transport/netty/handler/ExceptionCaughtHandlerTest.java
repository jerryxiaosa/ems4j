package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExceptionCaughtHandlerTest {

    @Test
    void exceptionCaught_whenTriggered_shouldCloseAndRemove() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        ExceptionCaughtHandler handler = new ExceptionCaughtHandler(channelManager);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.pipeline().fireExceptionCaught(new RuntimeException("boom"));

        Mockito.verify(channelManager).closeAndRemove(channel.id().asLongText());
    }
}
