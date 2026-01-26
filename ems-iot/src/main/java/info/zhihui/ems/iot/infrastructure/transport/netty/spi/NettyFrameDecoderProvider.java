package info.zhihui.ems.iot.infrastructure.transport.netty.spi;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import io.netty.channel.ChannelHandler;
import java.util.List;

/**
 * 协议解码器提供者。
 */
public interface NettyFrameDecoderProvider {

    /**
     * 探测 TCP 报文所属协议。
     *
     * @param payload 报文内容
     * @return 协议签名（无法识别时返回 null）
     */
    ProtocolSignature detectTcp(byte[] payload);

    /**
     * 创建该签名对应的 Netty 解码器链（顺序即 pipeline 顺序）。
     */
    List<ChannelHandler> createDecoders(ProtocolSignature signature);

    /**
     * 探测器优先级，值越小越优先。
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
