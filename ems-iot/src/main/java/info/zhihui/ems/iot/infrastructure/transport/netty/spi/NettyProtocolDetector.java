package info.zhihui.ems.iot.infrastructure.transport.netty.spi;

import info.zhihui.ems.iot.protocol.port.ProtocolSignature;

/**
 * 协议探测 SPI。
 */
public interface NettyProtocolDetector {

    /**
     * 探测 TCP 报文所属协议。
     *
     * @param payload 报文内容
     * @return 协议签名（无法识别时返回 null）
     */
    ProtocolSignature detectTcp(byte[] payload);

    /**
     * 探测器优先级，值越小越优先。
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
