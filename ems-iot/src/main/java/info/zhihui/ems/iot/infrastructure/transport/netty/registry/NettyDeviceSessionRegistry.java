package info.zhihui.ems.iot.infrastructure.transport.netty.registry;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.DeviceSessionRegistry;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Netty 设备会话注册实现。
 */
@Component
@RequiredArgsConstructor
public class NettyDeviceSessionRegistry implements DeviceSessionRegistry {

    private final ChannelManager channelManager;

    @Override
    public void register(Device device, ProtocolSession session) {
        if (device == null || session == null) {
            return;
        }
        if (!(session instanceof NettyProtocolSession nettySession)) {
            throw new IllegalArgumentException("仅支持 Netty 协议会话");
        }
        DeviceTypeEnum deviceType = device.getProduct() == null ? null : device.getProduct().getDeviceType();
        ChannelSession channelSession = new ChannelSession()
                .setDeviceNo(device.getDeviceNo())
                .setDeviceType(deviceType)
                .setChannel(nettySession.getChannel());
        channelManager.register(channelSession);
    }
}
