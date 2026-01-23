package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AcrelGatewayDeviceResolverTest {


    @Test
    void testResolveGateway_WhenContextHasDevice_ShouldReturnDevice() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        Device device = new Device().setDeviceNo("gw-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        context.setDeviceNo("gw-1");
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenReturn(device);

        Device result = resolver.resolveGateway(context);

        Assertions.assertSame(device, result);
    }

    @Test
    void testResolveGateway_WhenNoChannel_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry);

        Assertions.assertNull(resolver.resolveGateway(new SimpleProtocolMessageContext()));
        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testResolveGateway_WhenDeviceNoMissing_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(channel, new ChannelManager()));

        Assertions.assertNull(resolver.resolveGateway(context));
        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testResolveGateway_WhenDeviceFound_ShouldReturnDevice() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "gw-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Product product = new Product().setCode("P-1");
        Device device = new Device().setDeviceNo("gw-1").setProduct(product);
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenReturn(device);

        Device result = resolver.resolveGateway(context);

        Assertions.assertSame(device, result);
        Assertions.assertEquals("gw-1", context.getDeviceNo());
    }

    @Test
    void testResolveGateway_WhenRegistryThrows_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel, new ChannelManager());
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "gw-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenThrow(new IllegalStateException("fail"));

        Assertions.assertNull(resolver.resolveGateway(context));
    }
}
