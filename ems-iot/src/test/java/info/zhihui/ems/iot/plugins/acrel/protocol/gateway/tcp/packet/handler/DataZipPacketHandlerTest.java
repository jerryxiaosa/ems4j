package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.handler;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataZipPacketHandlerTest {

    @Test
    void command_shouldReturnDataZip() {
        DataPacketHandler delegate = Mockito.mock(DataPacketHandler.class);
        DataZipPacketHandler handler = new DataZipPacketHandler(delegate);

        Assertions.assertEquals(GatewayPacketCode.commandKey(GatewayPacketCode.DATA_ZIP), handler.command());
    }

    @Test
    void handle_shouldDelegateToDataHandler() {
        DataPacketHandler delegate = Mockito.mock(DataPacketHandler.class);
        DataZipPacketHandler handler = new DataZipPacketHandler(delegate);
        ProtocolMessageContext context = Mockito.mock(ProtocolMessageContext.class);
        AcrelMessage message = Mockito.mock(AcrelMessage.class);

        handler.handle(context, message);

        Mockito.verify(delegate).handle(context, message);
    }
}
