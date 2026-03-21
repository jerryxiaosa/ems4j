package info.zhihui.ems.iot.listener;

import info.zhihui.ems.iot.config.IotEnergyReportPushProperties;
import info.zhihui.ems.iot.domain.event.DeviceEnergyReportEvent;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolHeartbeatInboundEvent;
import info.zhihui.ems.iot.vo.StandardEnergyReportPushVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

class ProtocolInboundEventListenerTest {

    private static class StubDeviceRegistry implements DeviceRegistry {

        private final Device device;
        private Device updatedDevice;

        private StubDeviceRegistry(Device device) {
            this.device = device;
        }

        @Override
        public Integer save(Device device) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(Device device) {
            this.updatedDevice = device;
        }

        @Override
        public void deleteById(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Device getById(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Device getByDeviceNo(String deviceNo) {
            return device;
        }

        @Override
        public Device getByParentIdAndPortNoAndMeterAddress(Integer parentId, Integer portNo, Integer meterAddress) {
            throw new UnsupportedOperationException();
        }
    }

    private RestClient mockRestClient() {
        return Mockito.mock(RestClient.class);
    }

    private TaskExecutor directTaskExecutor() {
        return Runnable::run;
    }

    @Test
    void testHandleHeartbeat_DeviceNoMissing_ShouldSkipUpdate() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        ProtocolInboundEventListener listener =
                new ProtocolInboundEventListener(deviceRegistry, new IotEnergyReportPushProperties(), mockRestClient(), directTaskExecutor());
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
                new ProtocolInboundEventListener(deviceRegistry, new IotEnergyReportPushProperties(), mockRestClient(), directTaskExecutor());
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
        ProtocolInboundEventListener listener = new ProtocolInboundEventListener(deviceRegistry, properties, mockRestClient(), directTaskExecutor());

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

    @Test
    void testHandleEnergyReport_ShouldUpdateDeviceLastOnlineAt() {
        Device device = new Device().setDeviceNo("dev-1");
        StubDeviceRegistry deviceRegistry = new StubDeviceRegistry(device);
        IotEnergyReportPushProperties properties = new IotEnergyReportPushProperties();
        properties.setEnabled(false);
        ProtocolInboundEventListener listener =
                new ProtocolInboundEventListener(deviceRegistry, properties, null, null);
        LocalDateTime receivedAt = LocalDateTime.of(2026, 3, 20, 15, 30, 0);
        ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                .setDeviceNo("dev-1")
                .setReceivedAt(receivedAt)
                .setReportedAt(receivedAt)
                .setTotalEnergy(new BigDecimal("100"))
                .setHigherEnergy(new BigDecimal("20"))
                .setHighEnergy(new BigDecimal("20"))
                .setLowEnergy(new BigDecimal("20"))
                .setLowerEnergy(new BigDecimal("20"))
                .setDeepLowEnergy(new BigDecimal("20"));

        listener.handleEnergyReport(event);

        Assertions.assertEquals(receivedAt, device.getLastOnlineAt());
        Assertions.assertSame(device, deviceRegistry.updatedDevice);
    }

    @Test
    void testBuildPushVo_SourceReportIdShouldBeUuid() {
        ProtocolInboundEventListener listener =
                new ProtocolInboundEventListener(null, new IotEnergyReportPushProperties(), null, null);
        DeviceEnergyReportEvent reportEvent = new DeviceEnergyReportEvent()
                .setDeviceNo("dev-1")
                .setReportedAt(LocalDateTime.of(2026, 3, 20, 12, 0, 0))
                .setTotalEnergy(new BigDecimal("100"))
                .setHigherEnergy(new BigDecimal("20"))
                .setHighEnergy(new BigDecimal("20"))
                .setLowEnergy(new BigDecimal("20"))
                .setLowerEnergy(new BigDecimal("20"))
                .setDeepLowEnergy(new BigDecimal("20"));

        StandardEnergyReportPushVo pushVo =
                ReflectionTestUtils.invokeMethod(listener, "buildPushVo", reportEvent);

        Assertions.assertNotNull(pushVo);
        Assertions.assertDoesNotThrow(() -> UUID.fromString(pushVo.getSourceReportId()));
    }
}
