package info.zhihui.ems.iot.application.listener;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

class ProtocolInboundEventListenerTest {

    @Test
    void testHandleHeartbeat_DeviceNoMissing_ShouldSkipUpdate() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry);
        ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent().setDeviceNo(" ");

        listener.handleHeartbeat(event);

        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testHandleHeartbeat_DeviceNoPresent_ShouldUpdateLastOnlineAt() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        Device device = new Device().setDeviceNo("dev-1");
        Mockito.when(deviceRegistry.getByDeviceNo("dev-1")).thenReturn(device);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry);
        LocalDateTime receivedAt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent()
                .setDeviceNo("dev-1")
                .setReceivedAt(receivedAt);

        listener.handleHeartbeat(event);

        Mockito.verify(deviceRegistry).getByDeviceNo("dev-1");
        Mockito.verify(deviceRegistry).update(device);
        Assertions.assertEquals(receivedAt, device.getLastOnlineAt());
    }



}
