package info.zhihui.ems.iot.infrastructure.transport.netty.binder;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.infrastructure.transport.netty.session.NettyProtocolSession;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 通道设备绑定器（支持覆盖绑定）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyDeviceBinder implements DeviceBinder {

    private final DeviceRegistry deviceRegistry;
    private final ChannelManager channelManager;

    @Override
    public void bind(ProtocolMessageContext context, String deviceNo) {
        if (context == null || StringUtils.isBlank(deviceNo)) {
            return;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return;
        }
        if (!(session instanceof NettyProtocolSession nettySession)) {
            throw new IllegalArgumentException("仅支持 Netty 协议会话");
        }

        Device device = deviceRegistry.getByDeviceNo(deviceNo);
        DeviceTypeEnum deviceType = device.getProduct() == null ? null : device.getProduct().getDeviceType();
        ChannelSession channelSession = new ChannelSession()
                .setDeviceNo(device.getDeviceNo())
                .setDeviceType(deviceType)
                .setChannel(nettySession.getChannel());
        channelManager.register(channelSession);
        context.setDeviceNo(deviceNo);
        log.info("4G 绑定设备 deviceNo={} session={}", deviceNo, session.getSessionId());
    }
}
