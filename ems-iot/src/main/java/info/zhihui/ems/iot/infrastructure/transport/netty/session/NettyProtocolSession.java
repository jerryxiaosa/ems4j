package info.zhihui.ems.iot.infrastructure.transport.netty.session;

import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.protocol.port.ProtocolSessionKey;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;

import java.util.Objects;

/**
 * 基于 Netty Channel 的协议会话实现。
 */
public class NettyProtocolSession implements ProtocolSession {

    @Getter
    private final Channel channel;
    private final ChannelManager channelManager;

    public NettyProtocolSession(Channel channel, ChannelManager channelManager) {
        this.channel = channel;
        this.channelManager = Objects.requireNonNull(channelManager, "channelManager");
    }

    @Override
    public String getSessionId() {
        if (channel == null) {
            return null;
        }
        return channel.id().asLongText();
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void send(byte[] payload) {
        if (channel == null || payload == null) {
            return;
        }
        channelManager.sendDirectly(channel.id().asLongText(), payload);
    }

    @Override
    public void publishEvent(Object event) {
        if (channel == null || event == null) {
            return;
        }
        channel.pipeline().fireUserEventTriggered(event);
    }

    @Override
    public <T> T getAttribute(ProtocolSessionKey<T> key) {
        Attribute<T> attribute = getAttributeHandle(key);
        return attribute == null ? null : attribute.get();
    }

    @Override
    public <T> void setAttribute(ProtocolSessionKey<T> key, T value) {
        Attribute<T> attribute = getAttributeHandle(key);
        if (attribute != null) {
            attribute.set(value);
        }
    }

    @Override
    public <T> void removeAttribute(ProtocolSessionKey<T> key) {
        Attribute<T> attribute = getAttributeHandle(key);
        if (attribute != null) {
            attribute.set(null);
        }
    }

    @Override
    public void close() {
        if (channel == null) {
            return;
        }
        channelManager.closeAndRemove(channel.id().asLongText());
    }

    private <T> Attribute<T> getAttributeHandle(ProtocolSessionKey<T> key) {
        if (channel == null || key == null) {
            return null;
        }
        AttributeKey<T> attributeKey = AttributeKey.valueOf(key.name());
        return channel.attr(attributeKey);
    }
}
