package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {

    private final ChannelManager channelManager;

    public ExceptionCaughtHandler(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String channelId = ctx.channel().id().asLongText();
        log.info("通道 {} 处理异常，关闭通道", channelId, cause);
        channelManager.closeAndRemove(channelId);
    }
}
