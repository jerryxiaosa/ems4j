package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import io.netty.util.AttributeKey;

/**
 * Netty Channel Attribute，用于在通道上绑定协议签名。
 */
public final class ChannelAttributes {

    private ChannelAttributes() {
    }

    public static final AttributeKey<ProtocolSignature> PROTOCOL_SIGNATURE =
            AttributeKey.valueOf("protocolSignature");

    public static final AttributeKey<Integer> UNKNOWN_PROTOCOL_ATTEMPTS =
            AttributeKey.valueOf("unknownProtocolAttempts");

    public static final AttributeKey<Long> UNKNOWN_PROTOCOL_FIRST_SEEN_AT =
            AttributeKey.valueOf("unknownProtocolFirstSeenAt");

}
