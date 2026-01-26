package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.util.HexUtil;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.CommonProtocolSessionKeys;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class DataUploadPacketHandlerTest {

    @Test
    void command_shouldReturnDataUpload() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DeviceBinder binder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        DataUploadPacketHandler handler = new DataUploadPacketHandler(publisher, binder, codec);

        Assertions.assertEquals(Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD), handler.command());
    }

    @Test
    void handle_withSerial_shouldBindPublishAndAck() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DeviceBinder binder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        DataUploadPacketHandler handler = new DataUploadPacketHandler(publisher, binder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        LocalDateTime receivedAt = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(buildSession(channel))
                .setRawPayload(new byte[]{0x01, 0x02})
                .setReceivedAt(receivedAt)
                .setTransportType(TransportProtocolEnum.TCP);
        LocalDateTime time = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        DataUploadMessage message = new DataUploadMessage()
                .setSerialNumber("dev-1")
                .setMeterAddress("1-1")
                .setTotalEnergy(100)
                .setHigherEnergy(10)
                .setHighEnergy(20)
                .setLowEnergy(30)
                .setLowerEnergy(40)
                .setDeepLowEnergy(50)
                .setTime(time);

        handler.handle(context, message);

        Mockito.verify(binder).bind(context, "dev-1");
        ArgumentCaptor<ProtocolEnergyReportInboundEvent> captor = ArgumentCaptor.forClass(ProtocolEnergyReportInboundEvent.class);
        Mockito.verify(publisher).publish(captor.capture());
        ProtocolEnergyReportInboundEvent event = captor.getValue();
        Assertions.assertEquals("dev-1", event.getDeviceNo());
        Assertions.assertEquals("1-1", event.getMeterAddress());
        Assertions.assertEquals(new BigDecimal("1.00"), event.getTotalEnergy());
        Assertions.assertEquals(new BigDecimal("0.10"), event.getHigherEnergy());
        Assertions.assertEquals(new BigDecimal("0.20"), event.getHighEnergy());
        Assertions.assertEquals(new BigDecimal("0.30"), event.getLowEnergy());
        Assertions.assertEquals(new BigDecimal("0.40"), event.getLowerEnergy());
        Assertions.assertEquals(new BigDecimal("0.50"), event.getDeepLowEnergy());
        Assertions.assertEquals(time, event.getReportedAt());
        Assertions.assertEquals(receivedAt, event.getReceivedAt());
        Assertions.assertEquals(TransportProtocolEnum.TCP, event.getTransportType());
        Assertions.assertEquals(HexUtil.bytesToHexString(context.getRawPayload()), event.getRawPayload());

        byte[] outbound = readOutboundBytes(channel);
        Assertions.assertNotNull(outbound);
        Assertions.assertEquals(
                HexUtil.bytesToHexString(codec.encodeAck(Acrel4gPacketCode.DATA_UPLOAD)),
                HexUtil.bytesToHexString(outbound)
        );
    }

    @Test
    void handle_missingSerial_withChannelDeviceNo_shouldPublishAndAck() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DeviceBinder binder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        DataUploadPacketHandler handler = new DataUploadPacketHandler(publisher, binder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        ProtocolSession session = buildSession(channel);
        session.setAttribute(CommonProtocolSessionKeys.DEVICE_NO, "dev-2");
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(session)
                .setRawPayload(new byte[]{0x03})
                .setTransportType(TransportProtocolEnum.TCP);
        DataUploadMessage message = new DataUploadMessage()
                .setSerialNumber(null)
                .setMeterAddress("1-2")
                .setTotalEnergy(200);

        handler.handle(context, message);

        Mockito.verifyNoInteractions(binder);
        ArgumentCaptor<ProtocolEnergyReportInboundEvent> captor = ArgumentCaptor.forClass(ProtocolEnergyReportInboundEvent.class);
        Mockito.verify(publisher).publish(captor.capture());
        ProtocolEnergyReportInboundEvent event = captor.getValue();
        Assertions.assertEquals("dev-2", event.getDeviceNo());

        byte[] outbound = readOutboundBytes(channel);
        Assertions.assertNotNull(outbound);
    }

    @Test
    void handle_bindThrows_shouldSkipPublishAndAck() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DeviceBinder binder = Mockito.mock(DeviceBinder.class);
        Mockito.doThrow(new IllegalStateException("bind-fail"))
                .when(binder).bind(Mockito.any(), Mockito.anyString());
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        DataUploadPacketHandler handler = new DataUploadPacketHandler(publisher, binder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(buildSession(channel))
                .setRawPayload(new byte[]{0x01})
                .setTransportType(TransportProtocolEnum.TCP);
        DataUploadMessage message = new DataUploadMessage().setSerialNumber("dev-3");

        handler.handle(context, message);

        Mockito.verify(binder).bind(context, "dev-3");
        Mockito.verifyNoInteractions(publisher);
        Assertions.assertNull(channel.readOutbound());
    }

    @Test
    void handle_missingSerialAndChannelDeviceNo_shouldSkipPublishAndAck() {
        ProtocolInboundPublisher publisher = Mockito.mock(ProtocolInboundPublisher.class);
        DeviceBinder binder = Mockito.mock(DeviceBinder.class);
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        DataUploadPacketHandler handler = new DataUploadPacketHandler(publisher, binder, codec);
        EmbeddedChannel channel = new EmbeddedChannel();
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(buildSession(channel))
                .setRawPayload(new byte[]{0x01})
                .setTransportType(TransportProtocolEnum.TCP);
        DataUploadMessage message = new DataUploadMessage().setSerialNumber(" ");

        handler.handle(context, message);

        Mockito.verifyNoInteractions(binder, publisher);
        Assertions.assertNull(channel.readOutbound());
    }

    private byte[] readOutboundBytes(EmbeddedChannel channel) {
        Object outbound = channel.readOutbound();
        if (outbound == null) {
            return null;
        }
        if (!(outbound instanceof ByteBuf buf)) {
            ReferenceCountUtil.release(outbound);
            throw new AssertionError("outbound is not ByteBuf");
        }
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        ReferenceCountUtil.release(buf);
        return data;
    }

    private NettyProtocolSession buildSession(EmbeddedChannel channel) {
        ChannelManager manager = new ChannelManager();
        ChannelSession session = new ChannelSession().setChannel(channel);
        manager.register(session);
        return new NettyProtocolSession(channel, manager);
    }
}
