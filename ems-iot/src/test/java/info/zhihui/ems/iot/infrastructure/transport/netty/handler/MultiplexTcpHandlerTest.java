package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.protocol.port.registry.DeviceProtocolHandler;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.infrastructure.registry.DeviceProtocolHandlerRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelAttributes;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class MultiplexTcpHandlerTest {

    @Test
    void channelRead0_whenSignatureMissing_shouldSkip() {
        DeviceProtocolHandlerRegistry registry = Mockito.mock(DeviceProtocolHandlerRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        MultiplexTcpHandler handler = new MultiplexTcpHandler(registry, channelManager);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound(new byte[]{0x01, 0x02});

        Mockito.verifyNoInteractions(registry);
        Assertions.assertNull(channel.readInbound());
    }

    @Test
    void channelRead0_whenSignaturePresent_shouldRouteToHandler() {
        DeviceProtocolHandlerRegistry registry = Mockito.mock(DeviceProtocolHandlerRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        DeviceProtocolHandler protocolHandler = Mockito.mock(DeviceProtocolHandler.class);
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("ACREL")
                .setTransportType(TransportProtocolEnum.TCP);
        Mockito.when(registry.resolve(signature)).thenReturn(protocolHandler);
        MultiplexTcpHandler handler = new MultiplexTcpHandler(registry, channelManager);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        channel.attr(ChannelAttributes.PROTOCOL_SIGNATURE).set(signature);
        byte[] payload = new byte[]{0x11, 0x22};

        channel.writeInbound(payload);

        ArgumentCaptor<SimpleProtocolMessageContext> captor = ArgumentCaptor.forClass(SimpleProtocolMessageContext.class);
        Mockito.verify(protocolHandler).onMessage(captor.capture());
        SimpleProtocolMessageContext context = captor.getValue();
        Assertions.assertNotNull(context.getSession());
        Assertions.assertTrue(context.getSession() instanceof NettyProtocolSession);
        Assertions.assertSame(channel, ((NettyProtocolSession) context.getSession()).getChannel());
        Assertions.assertArrayEquals(payload, context.getRawPayload());
        Assertions.assertEquals(TransportProtocolEnum.TCP, context.getTransportType());
        Assertions.assertNotNull(context.getReceivedAt());
    }

    @Test
    void channelInactive_shouldRemoveSession() {
        DeviceProtocolHandlerRegistry registry = Mockito.mock(DeviceProtocolHandlerRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        MultiplexTcpHandler handler = new MultiplexTcpHandler(registry, channelManager);
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.close();

        Mockito.verify(channelManager).remove(channel.id().asLongText());
    }
}
