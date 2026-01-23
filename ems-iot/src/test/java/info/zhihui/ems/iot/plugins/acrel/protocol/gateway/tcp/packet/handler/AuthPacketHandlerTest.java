package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

class AuthPacketHandlerTest {

    @Test
    void command_shouldReturnAuth() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        AuthPacketHandler handler = new AuthPacketHandler(cryptoService, frameCodec, deviceRegistry, deviceBinder);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.AUTH), handler.command());
    }

    @Test
    void handle_whenRequestOperation_shouldSendSequenceAndStoreAttribute() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        ProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Device gateway = new Device().setDeviceNo("gw-1");
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenReturn(gateway);
        byte[] encoded = new byte[]{0x01, 0x02};
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.when(frameCodec.encode(Mockito.eq(GatewayPacketCode.AUTH), payloadCaptor.capture()))
                .thenReturn(encoded);
        AuthPacketHandler handler = new AuthPacketHandler(cryptoService, frameCodec, deviceRegistry, deviceBinder);
        GatewayAuthMessage message = new GatewayAuthMessage("gw-1", "b1", "request", null, null);

        handler.handle(context, message);

        Mockito.verify(deviceBinder).bind(Mockito.eq(context), Mockito.eq("gw-1"));
        ArgumentCaptor<String> sequenceCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(session).setAttribute(Mockito.eq(CommonProtocolSessionKeys.GATEWAY_AUTH_SEQUENCE), sequenceCaptor.capture());
        String sequence = sequenceCaptor.getValue();
        Assertions.assertNotNull(sequence);
        Assertions.assertTrue(sequence.matches("\\d{19,}"));
        String xml = new String(payloadCaptor.getValue(), StandardCharsets.UTF_8);
        Assertions.assertTrue(xml.contains("<building_id>b1</building_id>"));
        Assertions.assertTrue(xml.contains("<gateway_id>gw-1</gateway_id>"));
        Assertions.assertTrue(xml.contains("<sequence>" + sequence + "</sequence>"));
        Mockito.verify(session).send(encoded);
    }

    @Test
    void handle_whenMd5Mismatch_shouldCloseSession() {
        AcrelGatewayCryptoService cryptoService = Mockito.mock(AcrelGatewayCryptoService.class);
        AcrelGatewayFrameCodec frameCodec = Mockito.mock(AcrelGatewayFrameCodec.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceBinder deviceBinder = Mockito.mock(DeviceBinder.class);
        ProtocolSession session = Mockito.mock(ProtocolSession.class);
        Mockito.when(session.getAttribute(CommonProtocolSessionKeys.GATEWAY_AUTH_SEQUENCE)).thenReturn("seq-1");
        ProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("secret");
        Mockito.when(deviceRegistry.getByDeviceNo("gw-1")).thenReturn(gateway);
        Mockito.when(cryptoService.md5Hex("secretseq-1")).thenReturn("expected");
        byte[] encoded = new byte[]{0x03};
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.when(frameCodec.encode(Mockito.eq(GatewayPacketCode.AUTH), payloadCaptor.capture()))
                .thenReturn(encoded);
        AuthPacketHandler handler = new AuthPacketHandler(cryptoService, frameCodec, deviceRegistry, deviceBinder);
        GatewayAuthMessage message = new GatewayAuthMessage("gw-1", "b1", "md5", null, "wrong");

        handler.handle(context, message);

        String xml = new String(payloadCaptor.getValue(), StandardCharsets.UTF_8);
        Assertions.assertTrue(xml.contains("<result>fail</result>"));
        Mockito.verify(session).send(encoded);
        Mockito.verify(session).close();
    }
}
