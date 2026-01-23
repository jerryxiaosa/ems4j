package info.zhihui.ems.iot.infrastructure.transport.netty.binder;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class NettyDeviceBinderTest {

    @Test
    void bind_whenContextNull_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, channelManager);

        binder.bind(null, "dev-1");

        Mockito.verifyNoInteractions(registry, channelManager);
    }

    @Test
    void bind_whenChannelNull_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, channelManager);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();

        binder.bind(context, "dev-1");

        Mockito.verifyNoInteractions(registry, channelManager);
    }

    @Test
    void bind_whenDeviceNoBlank_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, channelManager);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(
                new NettyProtocolSession(new EmbeddedChannel(), new ChannelManager()));

        binder.bind(context, " ");

        Mockito.verifyNoInteractions(registry, channelManager);
    }

    @Test
    void bind_whenValid_shouldFillContextAndRegister() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        ChannelManager channelManager = Mockito.mock(ChannelManager.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, channelManager);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);

        Product product = new Product()
                .setCode("p-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC);
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(product);
        Mockito.when(registry.getByDeviceNo("dev-1")).thenReturn(device);

        binder.bind(context, "dev-1");

        Mockito.verify(registry).getByDeviceNo("dev-1");
        ArgumentCaptor<ChannelSession> channelCaptor = ArgumentCaptor.forClass(ChannelSession.class);
        Mockito.verify(channelManager).register(channelCaptor.capture());
        ChannelSession channelSession = channelCaptor.getValue();
        Assertions.assertEquals("dev-1", channelSession.getDeviceNo());
        Assertions.assertEquals(DeviceTypeEnum.ELECTRIC, channelSession.getDeviceType());
        Assertions.assertSame(channel, channelSession.getChannel());
        Assertions.assertEquals("dev-1", context.getDeviceNo());
        Assertions.assertEquals("dev-1", session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO));
    }
}
