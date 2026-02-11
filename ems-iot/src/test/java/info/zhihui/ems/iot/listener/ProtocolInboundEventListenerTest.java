package info.zhihui.ems.iot.listener;

import info.zhihui.ems.iot.config.IotEnergyReportPushProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class ProtocolInboundEventListenerTest {

    private RestClient mockRestClient() {
        return Mockito.mock(RestClient.class);
    }

    @Test
    void testHandleHeartbeat_DeviceNoMissing_ShouldSkipUpdate() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        ProtocolInboundEventListener listener =
                new ProtocolInboundEventListener(deviceRegistry, new IotEnergyReportPushProperties(), mockRestClient());
        ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent().setDeviceNo(" ");

        listener.handleHeartbeat(event);

        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testHandleHeartbeat_DeviceNoPresent_ShouldUpdateLastOnlineAt() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        Device device = new Device().setDeviceNo("dev-1");
        Mockito.when(deviceRegistry.getByDeviceNo("dev-1")).thenReturn(device);
        ProtocolInboundEventListener listener =
                new ProtocolInboundEventListener(deviceRegistry, new IotEnergyReportPushProperties(), mockRestClient());
        LocalDateTime receivedAt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent()
                .setDeviceNo("dev-1")
                .setReceivedAt(receivedAt);

        listener.handleHeartbeat(event);

        Mockito.verify(deviceRegistry).getByDeviceNo("dev-1");
        Mockito.verify(deviceRegistry).update(device);
        Assertions.assertEquals(receivedAt, device.getLastOnlineAt());
    }

    @Test
    void testHandleEnergyReport_DisabledPush_ShouldNotThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotEnergyReportPushProperties properties = new IotEnergyReportPushProperties();
        properties.setEnabled(false);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry, properties, mockRestClient());

        ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                .setDeviceNo("dev-1")
                .setReportedAt(LocalDateTime.of(2026, 2, 11, 10, 0, 0))
                .setTotalEnergy(new BigDecimal("100"))
                .setHigherEnergy(new BigDecimal("20"))
                .setHighEnergy(new BigDecimal("20"))
                .setLowEnergy(new BigDecimal("20"))
                .setLowerEnergy(new BigDecimal("20"))
                .setDeepLowEnergy(new BigDecimal("20"));

        Assertions.assertDoesNotThrow(() -> listener.handleEnergyReport(event));
    }
}
