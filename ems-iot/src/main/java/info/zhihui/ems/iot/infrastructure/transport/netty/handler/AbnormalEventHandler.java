package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbnormalEventHandler extends ChannelInboundHandlerAdapter {

    private final ChannelManager channelManager;

    public AbnormalEventHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof AbnormalEvent event)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        String channelId = ctx.channel().id().asLongText();
        boolean exceeded = channelManager.recordAbnormal(channelId, event.reason(), event.timestampMillis());
        if (event.forceClose() || exceeded) {
            log.warn("通道 {} 异常关闭，原因={} 详情={} 强制={}", channelId, event.reason(), event.detail(), event.forceClose());
            ctx.close();
            channelManager.remove(channelId);
            return;
        }
        ctx.fireUserEventTriggered(evt);
    }
}
