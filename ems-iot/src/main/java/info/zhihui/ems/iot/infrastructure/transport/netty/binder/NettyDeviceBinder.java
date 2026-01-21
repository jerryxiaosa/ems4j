package info.zhihui.ems.iot.infrastructure.transport.netty.binder;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.DeviceSessionRegistry;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
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
    private final DeviceSessionRegistry sessionRegistry;

    @Override
    public void bind(ProtocolMessageContext context, String deviceNo) {
        if (context == null || StringUtils.isBlank(deviceNo)) {
            return;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return;
        }
        Device device = deviceRegistry.getByDeviceNo(deviceNo);
        sessionRegistry.register(device, session);
        context.setDeviceNo(deviceNo);
        log.info("4G 绑定设备 deviceNo={} session={}", deviceNo, session.getSessionId());
    }
}
