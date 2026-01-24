package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketRegistry;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition.GatewayPacketDefinition;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class AcrelGatewayTcpInboundHandlerTest {

    @Test
    void handle_whenFrameTooShort_shouldFireAbnormalEvent() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        AcrelGatewayTcpInboundHandler handler = new AcrelGatewayTcpInboundHandler(codec, new GatewayPacketRegistry(Collections.emptyList()));
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
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        AcrelGatewayTcpInboundHandler handler = new AcrelGatewayTcpInboundHandler(codec, new GatewayPacketRegistry(Collections.emptyList()));
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
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        GatewayPacketDefinition definition = new GatewayPacketDefinition() {
            @Override
            public String command() {
                return GatewayPacketCode.commandKey((byte) 0x12);
            }

            @Override
            public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
                return null;
            }

            @Override
            public void handle(ProtocolMessageContext context, AcrelMessage message) {
            }
        };
        AcrelGatewayTcpInboundHandler handler = new AcrelGatewayTcpInboundHandler(codec, new GatewayPacketRegistry(List.of(definition)));
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
    void handle_whenParseSuccess_shouldInvokeDefinitionHandle() {
        AcrelGatewayFrameCodec codec = new AcrelGatewayFrameCodec();
        StubMessage message = new StubMessage();
        CaptureDefinition definition = new CaptureDefinition(GatewayPacketCode.commandKey((byte) 0x13), message);
        AcrelGatewayTcpInboundHandler handler = new AcrelGatewayTcpInboundHandler(codec, new GatewayPacketRegistry(List.of(definition)));
        EventCaptureHandler capture = new EventCaptureHandler();
        EmbeddedChannel channel = new EmbeddedChannel(capture);
        byte[] frame = codec.encode((byte) 0x13, new byte[]{0x01, 0x02});
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(channel, new ChannelManager()))
                .setRawPayload(frame);

        handler.handle(context);

        Assertions.assertTrue(definition.handled);
        Assertions.assertSame(context, definition.handledContext);
        Assertions.assertSame(message, definition.handledMessage);
        Assertions.assertNull(capture.event);
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

    private static final class StubMessage implements AcrelMessage {
    }

    private static final class CaptureDefinition implements GatewayPacketDefinition {

        private final String command;
        private final AcrelMessage message;
        private boolean handled;
        private ProtocolMessageContext handledContext;
        private AcrelMessage handledMessage;

        private CaptureDefinition(String command, AcrelMessage message) {
            this.command = command;
            this.message = message;
        }

        @Override
        public String command() {
            return command;
        }

        @Override
        public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
            return message;
        }

        @Override
        public void handle(ProtocolMessageContext context, AcrelMessage message) {
            handled = true;
            handledContext = context;
            handledMessage = message;
        }
    }
}
