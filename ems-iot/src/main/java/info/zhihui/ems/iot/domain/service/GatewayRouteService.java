package info.zhihui.ems.iot.domain.service;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.GatewayRoute;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 网关链路路由服务。
 */
@Service
@RequiredArgsConstructor
public class GatewayRouteService {

    private final DeviceRegistry deviceRegistry;

    /**
     * 获取设备对应的网关链路路由。
     *
     * @param device 设备
     * @return 网关路由
     */
    public GatewayRoute getRoute(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("设备不能为空");
        }
        Product product = device.getProduct();
        if (product == null) {
            throw new IllegalArgumentException("设备产品信息缺失");
        }
        if (!DeviceAccessModeEnum.GATEWAY.equals(product.getAccessMode())) {
            throw new IllegalArgumentException("设备不是网关链路设备");
        }
        if (DeviceTypeEnum.GATEWAY.equals(product.getDeviceType())) {
            return new GatewayRoute()
                    .setGateway(device)
                    .setTargetDevice(device)
                    .setGatewaySelf(true);
        }

        // 接入网关的设备再查询一次
        Integer parentId = device.getParentId();
        if (parentId == null) {
            throw new IllegalArgumentException("网关设备缺失");
        }
        Device gateway = deviceRegistry.getById(parentId);
        return new GatewayRoute()
                .setGateway(gateway)
                .setTargetDevice(device)
                .setGatewaySelf(false);
    }
}
