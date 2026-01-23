package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.protocol.port.registry.DeviceProtocolHandlerResolver;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommandAppService {

    private final DeviceRegistry deviceRegistry;
    private final DeviceProtocolHandlerResolver handlerResolver;

    public CompletableFuture<DeviceCommandResult> sendCommand(Integer deviceId, DeviceCommandRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("命令请求不能为空");
        }
        request.validate();
        Device device = deviceRegistry.getById(deviceId);
        Product product = device.getProduct();
        if (product == null || !StringUtils.hasText(product.getVendor())) {
            throw new IllegalArgumentException("设备产品信息缺失，无法下发命令");
        }
        if (product.getAccessMode() == null) {
            throw new IllegalArgumentException("设备接入方式缺失，无法下发命令");
        }
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(request.type())
                .setPayload(request);
        TransportProtocolEnum protocol = product.getProtocol();
        if (protocol == null || TransportProtocolEnum.TCP.equals(protocol)) {
            ProtocolSignature signature = buildSignature(device, product);
            command.setSignature(signature);
            return handlerResolver.resolve(signature).sendCommand(command);
        }

        throw new BusinessRuntimeException("暂不支持该协议:" + protocol);
    }

    private ProtocolSignature buildSignature(Device device, Product product) {
        if (DeviceAccessModeEnum.GATEWAY.equals(product.getAccessMode())) {
            Integer parentId = device.getParentId();
            if (parentId == null) {
                throw new IllegalArgumentException("网关设备缺失，无法下发命令");
            }
            Device gateway = deviceRegistry.getById(parentId);
            Product gatewayProduct = gateway.getProduct();
            if (gatewayProduct == null || !StringUtils.hasText(gatewayProduct.getVendor())) {
                throw new IllegalArgumentException("网关产品信息缺失，无法下发命令");
            }
            if (gatewayProduct.getAccessMode() == null) {
                throw new IllegalArgumentException("网关接入方式缺失，无法下发命令");
            }
            TransportProtocolEnum protocol = gatewayProduct.getProtocol();
            TransportProtocolEnum transport = protocol == null ? TransportProtocolEnum.TCP : protocol;
            return new ProtocolSignature()
                    .setVendor(gatewayProduct.getVendor())
                    .setProductCode(gatewayProduct.getCode())
                    .setAccessMode(gatewayProduct.getAccessMode())
                    .setTransportType(transport);
        }

        TransportProtocolEnum protocol = product.getProtocol();
        TransportProtocolEnum transport = protocol == null ? TransportProtocolEnum.TCP : protocol;
        return new ProtocolSignature()
                .setVendor(product.getVendor())
                .setProductCode(product.getCode())
                .setAccessMode(product.getAccessMode())
                .setTransportType(transport);
    }
}
