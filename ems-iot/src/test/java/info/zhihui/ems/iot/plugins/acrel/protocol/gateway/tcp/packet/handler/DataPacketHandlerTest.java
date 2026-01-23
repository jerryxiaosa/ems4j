package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayDataMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.MeterEnergyPayload;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayDeviceResolver;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.util.HexUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class DataPacketHandlerTest {

    @Test
    void handle_whenGatewayPresent_shouldPublishInboundEvent() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DataPacketHandler handler = new DataPacketHandler(deviceResolver, deviceRegistry, publisher);
        Device gateway = new Device().setId(1).setDeviceNo("gw-1");
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(gateway);
        Device meterDevice = new Device().setDeviceNo("meter-1");
        Mockito.when(deviceRegistry.getByParentIdAndPortNoAndMeterAddress(1, 1, 2)).thenReturn(meterDevice);
        LocalDateTime reportedAt = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        GatewayReportMessage report = new GatewayReportMessage("gw-1", reportedAt,
                List.of(new MeterEnergyPayload("01002", new BigDecimal("123.45"))));
        GatewayDataMessage message = new GatewayDataMessage(report, "<xml>data</xml>");
        ProtocolMessageContext context = buildContext();

        handler.handle(context, message);

        ArgumentCaptor<ProtocolEnergyReportInboundEvent> captor =
                ArgumentCaptor.forClass(ProtocolEnergyReportInboundEvent.class);
        Mockito.verify(publisher).publish(captor.capture());
        ProtocolEnergyReportInboundEvent event = captor.getValue();
        Assertions.assertEquals("meter-1", event.getDeviceNo());
        Assertions.assertEquals("gw-1", event.getGatewayDeviceNo());
        Assertions.assertEquals("2", event.getMeterAddress());
        Assertions.assertEquals(123, event.getTotalEnergy());
        Assertions.assertEquals(reportedAt, event.getReportedAt());
        Assertions.assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), event.getReceivedAt());
        Assertions.assertEquals(TransportProtocolEnum.TCP, event.getTransportType());
        Assertions.assertEquals("session-1", event.getSessionId());
        String rawHex = HexUtil.bytesToHexString(context.getRawPayload());
        Assertions.assertEquals("hex=" + rawHex + ";xml=<xml>data</xml>", event.getRawPayload());
    }

    @Test
    void handle_whenGatewayMissing_shouldSkipPublish() {
        AcrelGatewayDeviceResolver deviceResolver = Mockito.mock(AcrelGatewayDeviceResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        Mockito.when(deviceResolver.resolveGateway(Mockito.any())).thenReturn(null);
        DataPacketHandler handler = new DataPacketHandler(deviceResolver, deviceRegistry, publisher);
        ProtocolMessageContext context = buildContext();
        GatewayDataMessage message = new GatewayDataMessage(null, null);

        handler.handle(context, message);

        Mockito.verifyNoInteractions(deviceRegistry, publisher);
    }

    private ProtocolMessageContext buildContext() {
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getSessionId()).thenReturn("session-1");
        return new SimpleProtocolMessageContext()
                .setSession(session)
                .setRawPayload(new byte[]{0x01, 0x02})
                .setReceivedAt(LocalDateTime.of(2024, 2, 3, 4, 5, 6))
                .setTransportType(TransportProtocolEnum.TCP);
    }
}
