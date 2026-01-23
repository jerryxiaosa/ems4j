package info.zhihui.ems.iot.infrastructure.transport.netty.spi;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 协议解码器提供者。
 */
public interface NettyFrameDecoderProvider {

    /**
     * 是否支持指定协议签名。
     *
     * @param signature 协议签名
     * @return 是否支持
     */
    boolean supports(ProtocolSignature signature);

    /**
     * 创建该签名对应的 Netty 解码器链（顺序即 pipeline 顺序）。
     */
    List<ChannelHandler> createDecoders(ProtocolSignature signature);
}
