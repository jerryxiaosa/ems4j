package info.zhihui.ems.iot.infrastructure.transport.netty;

import info.zhihui.ems.iot.config.DeviceAdapterProperties;
import info.zhihui.ems.iot.infrastructure.registry.DeviceProtocolHandlerRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.handler.AbnormalEventHandler;
import info.zhihui.ems.iot.infrastructure.transport.netty.handler.ExceptionCaughtHandler;
import info.zhihui.ems.iot.infrastructure.transport.netty.handler.MultiplexTcpHandler;
import info.zhihui.ems.iot.infrastructure.transport.netty.handler.ProtocolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 单实例 Netty 服务器，收到报文后交给 {@link MultiplexTcpHandler}。
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("netty")
public class NettyServerBootstrap implements SmartLifecycle {

    private final DeviceAdapterProperties adapterProperties;
    private final DeviceProtocolHandlerRegistry handlerRegistry;
    private final ChannelManager channelManager;
    private final List<NettyProtocolDetector> protocolDetectors;
    private final List<NettyFrameDecoderProvider> frameDecoderProviders;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup businessGroup;
    private volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            return;
        }
        DeviceAdapterProperties.NettyProperties props = adapterProperties.getNetty();
        bossGroup = new NioEventLoopGroup(props.getBossThreads());
        workerGroup = new NioEventLoopGroup(props.getWorkerThreads());
        businessGroup = new DefaultEventExecutorGroup(resolveBusinessThreads(props),
                new DefaultThreadFactory("netty-biz"));
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtocolFrameDecoder(protocolDetectors, frameDecoderProviders,
                                props.getUnknownProtocol()));
                        pipeline.addLast(businessGroup, "abnormalEventHandler", new AbnormalEventHandler(channelManager));
                        pipeline.addLast(businessGroup, "multiplexTcpHandler",
                                new MultiplexTcpHandler(handlerRegistry, channelManager));
                        pipeline.addLast(businessGroup, "exceptionCaughtHandler", new ExceptionCaughtHandler(channelManager));
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(props.getPort()).sync();
            running = true;
            log.info("Netty server started on {}", future.channel().localAddress());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Netty server start interrupted", e);
        } catch (Exception ex) {
            stop();
            throw new IllegalStateException("Netty server start failed", ex);
        }
    }

    @Override
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (businessGroup != null) {
            businessGroup.shutdownGracefully();
            businessGroup = null;
        }
        running = false;
        log.info("Netty server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // 在其他组件就绪后再启动
    }

    private int resolveBusinessThreads(DeviceAdapterProperties.NettyProperties props) {
        int configured = props.getBusinessThreads();
        if (configured > 0) {
            return configured;
        }

        int cpuCores = Runtime.getRuntime().availableProcessors();
        return Math.max(cpuCores * 2, props.getWorkerThreads());
    }
}
