package info.zhihui.ems.iot.infrastructure.transport.netty.binder;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
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

class NettyDeviceBinderTest {

    @Test
    void bind_whenContextNull_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, sessionRegistry);

        binder.bind(null, "dev-1");

        Mockito.verifyNoInteractions(registry, sessionRegistry);
    }

    @Test
    void bind_whenChannelNull_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, sessionRegistry);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext();

        binder.bind(context, "dev-1");

        Mockito.verifyNoInteractions(registry, sessionRegistry);
    }

    @Test
    void bind_whenDeviceNoBlank_shouldSkip() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, sessionRegistry);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(new NettyProtocolSession(new EmbeddedChannel()));

        binder.bind(context, " ");

        Mockito.verifyNoInteractions(registry, sessionRegistry);
    }

    @Test
    void bind_whenValid_shouldFillContextAndRegister() {
        DeviceRegistry registry = Mockito.mock(DeviceRegistry.class);
        DeviceSessionRegistry sessionRegistry = Mockito.mock(DeviceSessionRegistry.class);
        NettyDeviceBinder binder = new NettyDeviceBinder(registry, sessionRegistry);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
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
        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<ProtocolSession> sessionCaptor = ArgumentCaptor.forClass(ProtocolSession.class);
        Mockito.verify(sessionRegistry).register(deviceCaptor.capture(), sessionCaptor.capture());

        Assertions.assertSame(device, deviceCaptor.getValue());
        Assertions.assertSame(session, sessionCaptor.getValue());
        Assertions.assertEquals("dev-1", context.getDeviceNo());
        Assertions.assertEquals("dev-1", session.getAttribute(CommonProtocolSessionKeys.DEVICE_NO));
    }
}
