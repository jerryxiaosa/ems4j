package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.port.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.CommonProtocolSessionKeys;
import info.zhihui.ems.iot.protocol.port.ProtocolCommandTransport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DownlinkAckMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DownlinkPacketHandlerTest {

    @Test
    void command_shouldReturnDownlink() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkPacketHandler handler = new DownlinkPacketHandler(commandTransport);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DOWNLINK), handler.command());
    }

    @Test
    void handle_withSerial_shouldCompletePendingWithModbusFrame() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkPacketHandler handler = new DownlinkPacketHandler(commandTransport);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel));
        byte[] modbusFrame = new byte[]{0x01, 0x02};
        DownlinkAckMessage message = new DownlinkAckMessage()
                .setSerialNumber("dev-1")
                .setModbusFrame(modbusFrame);

        handler.handle(context, message);

        Mockito.verify(commandTransport).completePending("dev-1", modbusFrame);
    }

    @Test
    void handle_missingSerial_shouldUseChannelDeviceNo() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkPacketHandler handler = new DownlinkPacketHandler(commandTransport);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = new NettyProtocolSession(channel);
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "dev-2");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext().setSession(session);
        byte[] modbusFrame = new byte[]{0x05, 0x06};
        DownlinkAckMessage message = new DownlinkAckMessage()
                .setSerialNumber(" ")
                .setModbusFrame(modbusFrame);

        handler.handle(context, message);

        Mockito.verify(commandTransport).completePending("dev-2", modbusFrame);
    }

    @Test
    void handle_withoutModbusFrame_shouldUseRawPayload() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkPacketHandler handler = new DownlinkPacketHandler(commandTransport);
        EmbeddedChannel channel = new EmbeddedChannel();
        byte[] rawPayload = new byte[]{0x11, 0x22};
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel))
                .setRawPayload(rawPayload);
        DownlinkAckMessage message = new DownlinkAckMessage()
                .setSerialNumber("dev-3")
                .setModbusFrame(null);

        handler.handle(context, message);

        Mockito.verify(commandTransport).completePending("dev-3", rawPayload);
    }

    @Test
    void handle_missingDeviceNo_shouldSkip() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DownlinkPacketHandler handler = new DownlinkPacketHandler(commandTransport);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel));
        DownlinkAckMessage message = new DownlinkAckMessage()
                .setSerialNumber(" ");

        handler.handle(context, message);

        Mockito.verifyNoInteractions(commandTransport);
    }
}
