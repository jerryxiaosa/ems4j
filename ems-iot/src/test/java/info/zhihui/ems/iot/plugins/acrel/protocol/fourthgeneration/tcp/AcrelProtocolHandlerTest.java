package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition.Acrel4gPacketDefinition;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.decode.ProtocolDecodeErrorEnum;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

class AcrelProtocolHandlerTest {

    @Test
    void handle_whenFrameTooShort_shouldFireAbnormalEvent() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        Acrel4gTcpInboundHandler handler = new Acrel4gTcpInboundHandler(new Acrel4gPacketRegistry(Collections.emptyList()), codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);

        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel, new ChannelManager()))
                .setRawPayload(new byte[]{0x01});

        handler.handle(context);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.FRAME_TOO_SHORT, capture.event.reason());
    }

    @Test
    void handle_whenUnknownCommand_shouldFireAbnormalEvent() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        Acrel4gTcpInboundHandler handler = new Acrel4gTcpInboundHandler(new Acrel4gPacketRegistry(Collections.emptyList()), codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);

        byte[] frame = codec.encode((byte) 0x11, new byte[0]);
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel, new ChannelManager()))
                .setRawPayload(frame);

        handler.handle(context);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.UNKNOWN_COMMAND, capture.event.reason());
    }

    @Test
    void handle_whenParseFailed_shouldFireAbnormalEvent() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        Acrel4gPacketDefinition definition = new Acrel4gPacketDefinition() {
            @Override
            public String command() {
                return Acrel4gPacketCode.commandKey((byte) 0x12);
            }

            @Override
            public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
                return null;
            }

            @Override
            public void handle(ProtocolMessageContext context, AcrelMessage message) {
            }
        };
        Acrel4gTcpInboundHandler handler = new Acrel4gTcpInboundHandler(new Acrel4gPacketRegistry(List.of(definition)), codec);
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);

        byte[] frame = codec.encode((byte) 0x12, new byte[]{0x01});
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel, new ChannelManager()))
                .setRawPayload(frame);

        handler.handle(context);

        Assertions.assertNotNull(capture.event);
        Assertions.assertEquals(AbnormalReasonEnum.PAYLOAD_PARSE_ERROR, capture.event.reason());
    }

    @Test
    void testParse_InvalidCrc_ShouldReturnReason() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        LocalDateTime time = LocalDateTime.of(2021, 10, 21, 19, 36, 42);
        byte[] frame = buildTimeSyncFrame(codec, time);

        frame[3] = (byte) (frame[3] ^ 0x01);

        FrameDecodeResult parsed = codec.decode(frame);
        Assertions.assertEquals(ProtocolDecodeErrorEnum.CRC_INVALID, parsed.reason());
    }

    @Test
    void testParse_FrameTooShort_ShouldReturnReason() {
        Acrel4gFrameCodec codec = new Acrel4gFrameCodec();
        byte[] frame = new byte[]{0x01, 0x02, 0x03};

        FrameDecodeResult parsed = codec.decode(frame);
        Assertions.assertEquals(ProtocolDecodeErrorEnum.FRAME_TOO_SHORT, parsed.reason());
    }

    private byte[] buildTimeSyncFrame(Acrel4gFrameCodec codec, LocalDateTime time) {
        return codec.encode(Acrel4gPacketCode.TIME_SYNC, buildTimeSyncBody(time));
    }

    private byte[] buildTimeSyncBody(LocalDateTime time) {
        byte[] body = new byte[7];
        int year = time.getYear() % 100;
        body[0] = (byte) year;
        body[1] = (byte) time.getMonthValue();
        body[2] = (byte) time.getDayOfMonth();
        int dayOfWeek = time.getDayOfWeek().getValue() % 7;
        body[3] = (byte) dayOfWeek;
        body[4] = (byte) time.getHour();
        body[5] = (byte) time.getMinute();
        body[6] = (byte) time.getSecond();
        return body;
    }

    private static class EventCaptureHandler extends ChannelInboundHandlerAdapter {

        private AbnormalEvent event;

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof AbnormalEvent abnormalEvent) {
                event = abnormalEvent;
            }
        }
    }
}
