package info.zhihui.ems.iot.application.listener;

import info.zhihui.ems.iot.domain.event.DeviceEnergyReportEvent;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.application.EnergyReportAppService;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;

class ProtocolInboundEventListenerTest {

    @Test
    void testHandleHeartbeat_DeviceNoMissing_ShouldSkipUpdate() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        EnergyReportAppService energyReportAppService = Mockito.mock(EnergyReportAppService.class);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry, energyReportAppService);
        ProtocolHeartbeatInboundEvent event = new ProtocolHeartbeatInboundEvent().setDeviceNo(" ");

        listener.handleHeartbeat(event);

        Mockito.verifyNoInteractions(deviceRegistry, energyReportAppService);
    }

    @Test
    void testHandleHeartbeat_DeviceNoPresent_ShouldUpdateLastOnlineAt() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        EnergyReportAppService energyReportAppService = Mockito.mock(EnergyReportAppService.class);
        Device device = new Device().setDeviceNo("dev-1");
        Mockito.when(deviceRegistry.getByDeviceNo("dev-1")).thenReturn(device);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry, energyReportAppService);
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
    void testHandleEnergyReport_ValidEvent_ShouldPublishReport() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        EnergyReportAppService energyReportAppService = Mockito.mock(EnergyReportAppService.class);
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry, energyReportAppService);
        LocalDateTime receivedAt = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        LocalDateTime reportedAt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                .setDeviceNo("dev-2")
                .setGatewayDeviceNo("gateway-1")
                .setMeterAddress("1-1")
                .setTotalEnergy(100)
                .setHigherEnergy(10)
                .setHighEnergy(20)
                .setLowEnergy(30)
                .setLowerEnergy(40)
                .setDeepLowEnergy(50)
                .setReceivedAt(receivedAt)
                .setReportedAt(reportedAt)
                .setTransportType(TransportProtocolEnum.TCP)
                .setRawPayload("0A0B");

        listener.handleEnergyReport(event);

        ArgumentCaptor<DeviceEnergyReportEvent> captor = ArgumentCaptor.forClass(DeviceEnergyReportEvent.class);
        Mockito.verify(energyReportAppService).handleEnergyReport(captor.capture());
        DeviceEnergyReportEvent reportEvent = captor.getValue();
        Assertions.assertEquals("dev-2", reportEvent.getDeviceNo());
        Assertions.assertEquals("gateway-1", reportEvent.getGatewayDeviceNo());
        Assertions.assertEquals("1-1", reportEvent.getMeterAddress());
        Assertions.assertEquals(100, reportEvent.getTotalEnergy());
        Assertions.assertEquals(10, reportEvent.getHigherEnergy());
        Assertions.assertEquals(20, reportEvent.getHighEnergy());
        Assertions.assertEquals(30, reportEvent.getLowEnergy());
        Assertions.assertEquals(40, reportEvent.getLowerEnergy());
        Assertions.assertEquals(50, reportEvent.getDeepLowEnergy());
        Assertions.assertEquals(receivedAt, reportEvent.getReceivedAt());
        Assertions.assertEquals(reportedAt, reportEvent.getReportedAt());
        Assertions.assertEquals("TCP", reportEvent.getSource());
        Assertions.assertEquals("0A0B", reportEvent.getRaw());
    }
}
