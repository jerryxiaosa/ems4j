package info.zhihui.ems.iot.infrastructure.transport.netty.handler;

import info.zhihui.ems.iot.config.DeviceAdapterProperties;
import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyFrameDecoderProvider;
import info.zhihui.ems.iot.infrastructure.transport.netty.spi.NettyProtocolDetector;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelAttributes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 轻量级探测器：负责在首次收到数据时识别协议并切换 pipeline。
 * - 探测成功后将签名写入 Channel Attribute 并安装对应解码器。
 * - 解码器由 {@link NettyFrameDecoderProvider} 提供，新增协议只需增加 provider。
 */
@Slf4j
public class ProtocolFrameDecoder extends ByteToMessageDecoder {

    private static final int SNAPSHOT_SIZE = 32;

    private final List<NettyProtocolDetector> protocolDetectors;
    private final List<NettyFrameDecoderProvider> decoderProviders;
    private final DeviceAdapterProperties.UnknownProtocolProperties unknownProtocolProperties;

    public ProtocolFrameDecoder(List<NettyProtocolDetector> protocolDetectors,
                                List<NettyFrameDecoderProvider> decoderProviders,
                                DeviceAdapterProperties.UnknownProtocolProperties unknownProtocolProperties) {
        this.protocolDetectors = new ArrayList<>(protocolDetectors);
        this.protocolDetectors.sort(Comparator.comparingInt(NettyProtocolDetector::getOrder)
                .thenComparing(detector -> detector.getClass().getName()));
        this.decoderProviders = decoderProviders;
        this.unknownProtocolProperties = unknownProtocolProperties;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Attribute<ProtocolSignature> attr = ctx.channel().attr(ChannelAttributes.PROTOCOL_SIGNATURE);
        if (!in.isReadable() || attr.get() != null) {
            return;
        }
        byte[] snapshot = snapshot(in);
        ProtocolSignature signature = detect(snapshot);
        if (signature == null) {
            handleUnknownProtocol(ctx, in);
            return;
        }
        clearUnknownProtocolState(ctx);
        attr.set(signature);
        if (!installDecoder(ctx, signature)) {
            log.warn("未找到协议 {} 对应的解码器，关闭通道 {}", signature, ctx.channel().id());
            ctx.close();
            return;
        }
        log.info("协议探测完成：vendor={} accessMode={} product={} channel={}", signature.getVendor(),
                signature.getAccessMode(), signature.getProductCode(), ctx.channel().id());
        if (in.isReadable()) {
            out.add(in.readRetainedSlice(in.readableBytes()));
        }
        ctx.pipeline().remove(this);
    }

    private ProtocolSignature detect(byte[] snapshot) {
        for (NettyProtocolDetector detector : protocolDetectors) {
            ProtocolSignature sig = detector.detectTcp(snapshot);
            if (sig != null) {
                return sig;
            }
        }
        return null;
    }

    private boolean installDecoder(ChannelHandlerContext ctx, ProtocolSignature sig) {
        Optional<NettyFrameDecoderProvider> provider = decoderProviders.stream()
                .filter(p -> p.supports(sig))
                .findFirst();
        if (provider.isEmpty()) {
            return false;
        }
        List<ChannelHandler> handlers = provider.get().createDecoders(sig);
        if (handlers == null || handlers.isEmpty()) {
            return false;
        }
        ChannelPipeline pipeline = ctx.pipeline();
        String previous = ctx.name();
        for (ChannelHandler handler : handlers) {
            pipeline.addAfter(previous, handler.getClass().getSimpleName(), handler);
            previous = handler.getClass().getSimpleName();
        }
        return true;
    }

    private byte[] snapshot(ByteBuf in) {
        int length = Math.min(in.readableBytes(), SNAPSHOT_SIZE);
        byte[] bytes = new byte[length];
        in.getBytes(in.readerIndex(), bytes);
        return bytes;
    }

    private void handleUnknownProtocol(ChannelHandlerContext ctx, ByteBuf in) {
        long now = System.currentTimeMillis();
        Attribute<Long> firstSeenAttr = ctx.channel().attr(ChannelAttributes.UNKNOWN_PROTOCOL_FIRST_SEEN_AT);
        Long firstSeenAt = firstSeenAttr.get();
        if (firstSeenAt == null) {
            firstSeenAt = now;
            firstSeenAttr.set(firstSeenAt);
        }
        Attribute<Integer> attemptsAttr = ctx.channel().attr(ChannelAttributes.UNKNOWN_PROTOCOL_ATTEMPTS);
        Integer attempts = attemptsAttr.get();
        attempts = (attempts == null ? 0 : attempts) + 1;
        attemptsAttr.set(attempts);

        int readableBytes = in.readableBytes();
        long elapsed = now - firstSeenAt;
        int maxBytes = unknownProtocolProperties.getMaxBytes();
        int maxAttempts = unknownProtocolProperties.getMaxAttempts();
        long maxDurationMs = unknownProtocolProperties.getMaxDurationMs();
        boolean overBytes = maxBytes > 0 && readableBytes >= maxBytes;
        boolean overAttempts = maxAttempts > 0 && attempts >= maxAttempts;
        boolean overDuration = maxDurationMs > 0 && elapsed >= maxDurationMs;
        if (overBytes || overAttempts || overDuration) {
            log.warn("协议探测失败，关闭通道 {}，readableBytes={} attempts={} elapsedMs={}",
                    ctx.channel().id(), readableBytes, attempts, elapsed);
            in.skipBytes(readableBytes);
            ctx.close();
        }
    }

    private void clearUnknownProtocolState(ChannelHandlerContext ctx) {
        ctx.channel().attr(ChannelAttributes.UNKNOWN_PROTOCOL_ATTEMPTS).set(null);
        ctx.channel().attr(ChannelAttributes.UNKNOWN_PROTOCOL_FIRST_SEEN_AT).set(null);
    }
}
