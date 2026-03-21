package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.domain.command.concrete.GetCtCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.domain.service.GatewayRouteService;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.registry.DeviceProtocolHandler;
import info.zhihui.ems.iot.protocol.port.registry.DeviceProtocolHandlerResolver;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

class CommandAppServiceTest {

    @Test
    void testSendCommand_WhenGatewayChild_ShouldUseGatewayProductBuildSignature() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        DeviceProtocolHandlerResolver handlerResolver = Mockito.mock(DeviceProtocolHandlerResolver.class);
        GatewayRouteService gatewayRouteService = new GatewayRouteService(deviceRegistry);
        CommandAppService commandAppService = new CommandAppService(deviceRegistry, handlerResolver, gatewayRouteService);

        Device childDevice = new Device()
                .setId(2)
                .setDeviceNo("meter-1")
                .setParentId(1)
                .setProduct(new Product()
                        .setVendor("CHILD")
                        .setCode("CHILD_PRODUCT")
                        .setDeviceType(DeviceTypeEnum.ELECTRIC)
                        .setAccessMode(DeviceAccessModeEnum.GATEWAY));
        Device gateway = new Device()
                .setId(1)
                .setDeviceNo("gw-1")
                .setProduct(new Product()
                        .setVendor("ACREL")
                        .setCode("ACREL_GATEWAY")
                        .setDeviceType(DeviceTypeEnum.GATEWAY)
                        .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                        .setProtocol(TransportProtocolEnum.TCP));
        DeviceProtocolHandler handler = Mockito.mock(DeviceProtocolHandler.class);
        DeviceCommandResult expected = new DeviceCommandResult()
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setSuccess(true);
        Mockito.when(deviceRegistry.getById(2)).thenReturn(childDevice);
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);
        Mockito.when(handlerResolver.resolve(Mockito.any())).thenReturn(handler);
        Mockito.when(handler.sendCommand(Mockito.any(DeviceCommand.class)))
                .thenReturn(CompletableFuture.completedFuture(expected));

        DeviceCommandResult result = commandAppService.sendCommand(2, new GetCtCommand()).join();

        Assertions.assertSame(expected, result);
        ArgumentCaptor<ProtocolSignature> signatureCaptor = ArgumentCaptor.forClass(ProtocolSignature.class);
        Mockito.verify(handlerResolver).resolve(signatureCaptor.capture());
        ProtocolSignature signature = signatureCaptor.getValue();
        Assertions.assertEquals("ACREL", signature.getVendor());
        Assertions.assertEquals("ACREL_GATEWAY", signature.getProductCode());
        Assertions.assertEquals(DeviceAccessModeEnum.GATEWAY, signature.getAccessMode());
        Assertions.assertEquals(TransportProtocolEnum.TCP, signature.getTransportType());
    }
}
