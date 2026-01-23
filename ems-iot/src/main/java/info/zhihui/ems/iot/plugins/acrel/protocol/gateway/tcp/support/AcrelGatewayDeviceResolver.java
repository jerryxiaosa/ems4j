package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.DeviceBinder;
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
            return deviceRegistry.getByDeviceNo(deviceNo);
        } catch (Exception ex) {
            return null;
        }
    }
}
