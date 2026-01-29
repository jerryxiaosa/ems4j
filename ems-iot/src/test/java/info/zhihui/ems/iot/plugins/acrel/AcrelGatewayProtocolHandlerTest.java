package info.zhihui.ems.iot.plugins.acrel;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.constant.AcrelPluginConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.AcrelGatewayTcpCommandSender;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.AcrelGatewayTcpInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

class AcrelGatewayProtocolHandlerTest {

    @Test
    void testGetVendor_Default_ReturnsAcrelVendor() {
        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(
                Mockito.mock(AcrelGatewayTcpInboundHandler.class),
                Mockito.mock(AcrelGatewayTcpCommandSender.class));

        Assertions.assertEquals(AcrelPluginConstants.VENDOR, handler.getVendor());
    }

    @Test
    void testGetAccessMode_Default_ReturnsGateway() {
        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(
                Mockito.mock(AcrelGatewayTcpInboundHandler.class),
                Mockito.mock(AcrelGatewayTcpCommandSender.class));

        Assertions.assertEquals(DeviceAccessModeEnum.GATEWAY, handler.getAccessMode());
    }

    @Test
    void sendCommand_shouldDelegateToCommandSender() {
        AcrelGatewayTcpInboundHandler inboundHandler = Mockito.mock(AcrelGatewayTcpInboundHandler.class);
        AcrelGatewayTcpCommandSender commandSender = Mockito.mock(AcrelGatewayTcpCommandSender.class);
        DeviceCommand command = new DeviceCommand();
        CompletableFuture<DeviceCommandResult> expected = CompletableFuture.completedFuture(new DeviceCommandResult());
        Mockito.when(commandSender.send(command)).thenReturn(expected);

        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(inboundHandler, commandSender);
        CompletableFuture<DeviceCommandResult> result = handler.sendCommand(command);

        Assertions.assertSame(expected, result);
        Mockito.verify(commandSender).send(command);
    }

    @Test
    void onMessage_whenGateway_shouldRouteToGatewayHandler() {
        AcrelGatewayTcpInboundHandler inboundHandler = Mockito.mock(AcrelGatewayTcpInboundHandler.class);
        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(
                inboundHandler, Mockito.mock(AcrelGatewayTcpCommandSender.class));
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(TransportProtocolEnum.TCP)
                .setSession(new NettyProtocolSession(channel, new ChannelManager()));

        handler.onMessage(context);

        Mockito.verify(inboundHandler).handle(context);
    }

    @Test
    void onMessage_whenTransportNotTcp_shouldThrow() {
        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(
                Mockito.mock(AcrelGatewayTcpInboundHandler.class),
                Mockito.mock(AcrelGatewayTcpCommandSender.class));
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(TransportProtocolEnum.MQTT);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> handler.onMessage(context));
    }

    @Test
    void onMessage_whenTransportNull_shouldThrow() {
        AcrelGatewayProtocolHandler handler = new AcrelGatewayProtocolHandler(
                Mockito.mock(AcrelGatewayTcpInboundHandler.class),
                Mockito.mock(AcrelGatewayTcpCommandSender.class));
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(null);

        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class, () -> handler.onMessage(context));
        Assertions.assertTrue(exception.getMessage().contains("null"));
    }
}
