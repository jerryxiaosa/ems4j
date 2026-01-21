package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.DeviceSessionRegistry;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.CommonProtocolSessionKeys;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class AcrelGatewayDeviceResolverTest {

    @Test
    void testBindGateway_WhenGatewayIdBlank_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);

        Device device = resolver.bindGateway(new SimpleProtocolMessageContext(), " ");

        Assertions.assertNull(device);
        Mockito.verifyNoInteractions(deviceRegistry, sessionRegistry);
    }

    @Test
    void testBindGateway_WhenContextNull_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);

        Device device = resolver.bindGateway(null, "gw-1");

        Assertions.assertNull(device);
        Mockito.verifyNoInteractions(deviceRegistry, sessionRegistry);
    }

    @Test
    void testBindGateway_WhenChannelNull_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);

        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();
        Device device = resolver.bindGateway(context, "gw-1");

        Assertions.assertNull(device);
        Mockito.verifyNoInteractions(deviceRegistry, sessionRegistry);
    }

    @Test
    void testBindGateway_WhenDeviceFound_ShouldRegisterSession() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Product product = new Product().setCode("P-1");
        Device device = new Device().setDeviceNo("gw-1").setProduct(product);
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenReturn(device);
        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<ProtocolSession> sessionCaptor = ArgumentCaptor.forClass(ProtocolSession.class);

        Device result = resolver.bindGateway(context, "gw-1");

        Assertions.assertSame(device, result);
        Mockito.verify(sessionRegistry).register(deviceCaptor.capture(), sessionCaptor.capture());
        Assertions.assertSame(device, deviceCaptor.getValue());
        Assertions.assertSame(session, sessionCaptor.getValue());
        Assertions.assertEquals("gw-1", context.getDeviceNo());
        Assertions.assertEquals("gw-1", session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO));
    }

    @Test
    void testBindGateway_WhenRegistryThrows_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(channel));
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenThrow(new IllegalStateException("fail"));

        Device result = resolver.bindGateway(context, "gw-1");

        Assertions.assertNull(result);
        Mockito.verifyNoInteractions(sessionRegistry);
    }

    @Test
    void testResolveGateway_WhenContextHasDevice_ShouldReturnDevice() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
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
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);

        Assertions.assertNull(resolver.resolveGateway(new SimpleProtocolMessageContext()));
        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testResolveGateway_WhenDeviceNoMissing_ShouldReturnNull() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(channel));

        Assertions.assertNull(resolver.resolveGateway(context));
        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testResolveGateway_WhenDeviceFound_ShouldReturnDevice() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
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
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        AcrelGatewayDeviceResolver resolver = new AcrelGatewayDeviceResolver(deviceRegistry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "gw-1");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenThrow(new IllegalStateException("fail"));

        Assertions.assertNull(resolver.resolveGateway(context));
    }
}
