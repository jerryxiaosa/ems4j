package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.infrastructure.registry.DeviceProtocolHandlerRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelAttributes;
import info.zhihui.ems.iot.protocol.port.inbound.SimpleProtocolMessageContext;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class MultiplexTcpHandler extends SimpleChannelInboundHandler<byte[]> {

    private final DeviceProtocolHandlerRegistry handlerRegistry;
    private final ChannelManager channelManager;

    public MultiplexTcpHandler(DeviceProtocolHandlerRegistry handlerRegistry, ChannelManager channelManager) {
        this.handlerRegistry = handlerRegistry;
        this.channelManager = channelManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Netty 通道建立，channel={}", ctx.channel().id());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] payload) {
        ProtocolSignature signature = ctx.channel().attr(ChannelAttributes.PROTOCOL_SIGNATURE).get();
        if (signature == null) {
            log.warn("通道 {} 尚未绑定协议签名，丢弃当前报文", ctx.channel().id());
            return;
        }
        SimpleProtocolMessageContext context = new SimpleProtocolMessageContext()
                .setSession(new NettyProtocolSession(ctx.channel(), channelManager))
                .setRawPayload(payload)
                .setReceivedAt(LocalDateTime.now())
                .setTransportType(TransportProtocolEnum.TCP);

        try {
            handlerRegistry.resolve(signature).onMessage(context);
        } catch (RuntimeException ex) {
            log.warn("通道 {} 处理报文异常，signature={} session={}",
                    ctx.channel().id(), signature, context.getSession() == null ? null : context.getSession().getSessionId(), ex);
            if (context.getSession() == null) {
                return;
            }
            context.getSession().publishEvent(
                    new AbnormalEvent(AbnormalReasonEnum.BUSINESS_ERROR, System.currentTimeMillis(), "处理报文异常")
            );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channelManager.remove(ctx.channel().id().asLongText());
        log.info("Netty 通道关闭，channel={}", ctx.channel().id());
    }
}
