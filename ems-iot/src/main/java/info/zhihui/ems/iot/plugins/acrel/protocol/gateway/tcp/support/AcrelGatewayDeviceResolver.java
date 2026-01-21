package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.DeviceSessionRegistry;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 网关设备解析与绑定器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AcrelGatewayDeviceResolver {

    private final DeviceRegistry deviceRegistry;
    private final DeviceSessionRegistry sessionRegistry;

    public Device bindGateway(ProtocolMessageContext context, String gatewayId) {
        if (!StringUtils.hasText(gatewayId) || context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return null;
        }
        try {
            Device device = deviceRegistry.getByDeviceNo(gatewayId);
            sessionRegistry.register(device, session);
            context.setDeviceNo(device.getDeviceNo());
            return device;
        } catch (Exception ex) {
            log.warn("网关绑定失败，gatewayId={}", gatewayId);
            return null;
        }
    }

    public Device resolveGateway(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return null;
        }
        String deviceNo = context.getDeviceNo();
        if (!StringUtils.hasText(deviceNo)) {
            return null;
        }
        try {
            Device device = deviceRegistry.getByDeviceNo(deviceNo);
            return device;
        } catch (Exception ex) {
            return null;
        }
    }
}
