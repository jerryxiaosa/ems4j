package info.zhihui.ems.iot.plugins.acrel;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.Acrel4gTcpInboundHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.Acrel4gTcpCommandSender;
import info.zhihui.ems.iot.plugins.acrel.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

class Acrel4gProtocolHandlerTest {

    @Test
    void testGetVendor_Default_ReturnsAcrelVendor() {
        Acrel4gProtocolHandler handler = buildHandler();

        Assertions.assertEquals(AcrelProtocolConstants.VENDOR, handler.getVendor());
    }

    @Test
    void testGetAccessMode_Default_ReturnsDirect() {
        Acrel4gProtocolHandler handler = buildHandler();

        Assertions.assertEquals(DeviceAccessModeEnum.DIRECT, handler.getAccessMode());
    }

    @Test
    void onMessage_whenDirect_shouldRouteTo4gHandler() {
        Acrel4gTcpInboundHandler inboundHandler = Mockito.mock(Acrel4gTcpInboundHandler.class);
        Acrel4gProtocolHandler handler = buildHandler(inboundHandler, Mockito.mock(Acrel4gTcpCommandSender.class));
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(TransportProtocolEnum.TCP)
                .setSession(new NettyProtocolSession(channel, new ChannelManager()));

        handler.onMessage(context);

        Mockito.verify(inboundHandler).handle(context);
    }

    @Test
    void onMessage_whenTransportNotTcp_shouldThrow() {
        Acrel4gProtocolHandler handler = buildHandler();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(TransportProtocolEnum.MQTT);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> handler.onMessage(context));
    }

    @Test
    void testOnMessage_TransportNull_ThrowsUnsupportedOperationException() {
        Acrel4gProtocolHandler handler = buildHandler();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setTransportType(null);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> handler.onMessage(context));
    }

    @Test
    void testSendCommand_CommandProvided_DelegatesToSender() {
        Acrel4gTcpInboundHandler inboundHandler = Mockito.mock(Acrel4gTcpInboundHandler.class);
        Acrel4gTcpCommandSender commandSender = Mockito.mock(Acrel4gTcpCommandSender.class);
        DeviceCommand command = new DeviceCommand();
        CompletableFuture<DeviceCommandResult> expected = CompletableFuture.completedFuture(new DeviceCommandResult());
        Mockito.when(commandSender.send(command)).thenReturn(expected);
        Acrel4gProtocolHandler handler = buildHandler(inboundHandler, commandSender);

        CompletableFuture<DeviceCommandResult> result = handler.sendCommand(command);

        Assertions.assertSame(expected, result);
        Mockito.verify(commandSender).send(command);
    }

    private Acrel4gProtocolHandler buildHandler() {
        return buildHandler(Mockito.mock(Acrel4gTcpInboundHandler.class), Mockito.mock(Acrel4gTcpCommandSender.class));
    }

    private Acrel4gProtocolHandler buildHandler(Acrel4gTcpInboundHandler inboundHandler,
                                                Acrel4gTcpCommandSender commandSender) {
        return new Acrel4gProtocolHandler(inboundHandler, commandSender);
    }
}
