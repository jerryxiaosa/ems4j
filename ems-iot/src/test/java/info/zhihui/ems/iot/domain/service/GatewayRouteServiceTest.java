package info.zhihui.ems.iot.domain.service;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.GatewayRoute;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GatewayRouteServiceTest {

    @Test
    void testGetRoute_WhenGatewaySelf_ShouldReturnSelfRoute() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        GatewayRouteService gatewayRouteService = new GatewayRouteService(deviceRegistry);
        Device gateway = new Device()
                .setId(1)
                .setDeviceNo("gw-1")
                .setProduct(new Product()
                        .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                        .setDeviceType(DeviceTypeEnum.GATEWAY));

        GatewayRoute route = gatewayRouteService.getRoute(gateway);

        Assertions.assertSame(gateway, route.getGateway());
        Assertions.assertSame(gateway, route.getTargetDevice());
        Assertions.assertTrue(route.isGatewaySelf());
        Mockito.verifyNoInteractions(deviceRegistry);
    }

    @Test
    void testGetRoute_WhenGatewayChild_ShouldReturnParentGateway() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        GatewayRouteService gatewayRouteService = new GatewayRouteService(deviceRegistry);
        Device childDevice = new Device()
                .setId(2)
                .setDeviceNo("meter-1")
                .setParentId(1)
                .setProduct(new Product()
                        .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                        .setDeviceType(DeviceTypeEnum.ELECTRIC));
        Device gateway = new Device().setId(1).setDeviceNo("gw-1");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

        GatewayRoute route = gatewayRouteService.getRoute(childDevice);

        Assertions.assertSame(gateway, route.getGateway());
        Assertions.assertSame(childDevice, route.getTargetDevice());
        Assertions.assertFalse(route.isGatewaySelf());
    }

    @Test
    void testGetRoute_WhenParentIdMissing_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        GatewayRouteService gatewayRouteService = new GatewayRouteService(deviceRegistry);
        Device childDevice = new Device()
                .setId(2)
                .setDeviceNo("meter-1")
                .setProduct(new Product()
                        .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                        .setDeviceType(DeviceTypeEnum.ELECTRIC));

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> gatewayRouteService.getRoute(childDevice));

        Assertions.assertEquals("网关设备缺失", exception.getMessage());
    }

    @Test
    void testGetRoute_WhenAccessModeNotGateway_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        GatewayRouteService gatewayRouteService = new GatewayRouteService(deviceRegistry);
        Device device = new Device()
                .setId(1)
                .setDeviceNo("dev-1")
                .setProduct(new Product()
                        .setAccessMode(DeviceAccessModeEnum.DIRECT)
                        .setDeviceType(DeviceTypeEnum.ELECTRIC));

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> gatewayRouteService.getRoute(device));

        Assertions.assertEquals("设备不是网关链路设备", exception.getMessage());
    }
}
