package info.zhihui.ems.iot.infrastructure.transport.netty.registry;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class NettyDeviceSessionRegistryTest {

    @Test
    void testRegister_DeviceNull_Ignores() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceSessionRegistry registry = new NettyDeviceSessionRegistry(channelManager);

        registry.register(null, Mockito.mock(ProtocolSession.class));

        Mockito.verifyNoInteractions(channelManager);
    }

    @Test
    void testRegister_SessionNull_Ignores() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceSessionRegistry registry = new NettyDeviceSessionRegistry(channelManager);

        registry.register(new Device().setDeviceNo("dev-1"), null);

        Mockito.verifyNoInteractions(channelManager);
    }

    @Test
    void testRegister_SessionNotNetty_ThrowsIllegalArgumentException() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceSessionRegistry registry = new NettyDeviceSessionRegistry(channelManager);
        Device device = new Device().setDeviceNo("dev-1");
        ProtocolSession session = Mockito.mock(ProtocolSession.class);

        Assertions.assertThrows(IllegalArgumentException.class, () -> registry.register(device, session));
        Mockito.verifyNoInteractions(channelManager);
    }

    @Test
    void testRegister_WithProduct_PopulatesChannelSession() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceSessionRegistry registry = new NettyDeviceSessionRegistry(channelManager);
        EmbeddedChannel channel = new EmbeddedChannel();
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(new Product().setDeviceType(DeviceTypeEnum.ELECTRIC));
        NettyProtocolSession session = new NettyProtocolSession(channel);

        registry.register(device, session);

        ArgumentCaptor<ChannelSession> captor = ArgumentCaptor.forClass(ChannelSession.class);
        Mockito.verify(channelManager).register(captor.capture());
        ChannelSession captured = captor.getValue();
        Assertions.assertEquals("dev-1", captured.getDeviceNo());
        Assertions.assertEquals(DeviceTypeEnum.ELECTRIC, captured.getDeviceType());
        Assertions.assertSame(channel, captured.getChannel());
    }

    @Test
    void testRegister_ProductNull_DeviceTypeNull() {
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceSessionRegistry registry = new NettyDeviceSessionRegistry(channelManager);
        EmbeddedChannel channel = new EmbeddedChannel();
        Device device = new Device().setDeviceNo("dev-2");
        NettyProtocolSession session = new NettyProtocolSession(channel);

        registry.register(device, session);

        ArgumentCaptor<ChannelSession> captor = ArgumentCaptor.forClass(ChannelSession.class);
        Mockito.verify(channelManager).register(captor.capture());
        ChannelSession captured = captor.getValue();
        Assertions.assertEquals("dev-2", captured.getDeviceNo());
        Assertions.assertNull(captured.getDeviceType());
        Assertions.assertSame(channel, captured.getChannel());
    }
}
