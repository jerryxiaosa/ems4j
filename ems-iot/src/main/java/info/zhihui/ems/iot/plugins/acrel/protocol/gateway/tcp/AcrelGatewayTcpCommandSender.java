package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.config.IotCommandProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.port.DeviceCommandTranslator;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayMeterIdCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.protocol.port.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.protocol.port.ProtocolCommandTransport;
import info.zhihui.ems.iot.plugins.acrel.support.DeviceCommandSupport;
import info.zhihui.ems.iot.util.ProtocolTimeoutSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 安科瑞网关 TCP 下行命令发送器。
 */
@Component
@RequiredArgsConstructor
public class AcrelGatewayTcpCommandSender {

    private final ProtocolCommandTransport commandTransport;
    private final DeviceCommandTranslatorResolver translatorRegistry;
    private final IotCommandProperties commandProperties;
    private final DeviceRegistry deviceRegistry;
    private final AcrelGatewayFrameCodec gatewayFrameCodec;
    private final AcrelGatewayCryptoService gatewayCryptoService;
    private final AcrelGatewayTransparentCodec gatewayTransparentCodec;

    public CompletableFuture<DeviceCommandResult> send(DeviceCommand command) {
        Device device = DeviceCommandSupport.requireDevice(command, DeviceAccessModeEnum.GATEWAY);
        Integer parentId = device.getParentId();
        if (parentId == null) {
            throw new IllegalArgumentException("网关设备缺失，deviceNo=" + device.getDeviceNo());
        }
        Device gateway = deviceRegistry.getById(parentId);
        if (!StringUtils.hasText(gateway.getDeviceSecret())) {
            throw new IllegalArgumentException("网关密钥缺失，deviceNo=" + gateway.getDeviceNo());
        }
        if (device.getPortNo() == null || device.getMeterAddress() == null) {
            throw new IllegalArgumentException("子设备标识缺失，deviceNo=" + device.getDeviceNo());
        }
        DeviceCommandTranslator<ModbusRtuRequest> translator = translatorRegistry.resolve(
                device.getProduct().getVendor(), device.getProduct().getCode(), command.getType(), ModbusRtuRequest.class);
        ModbusRtuRequest rtuRequest = translator.toRequest(command);
        byte[] rtuFrame = ModbusRtuBuilder.build(rtuRequest);
        String meterId = AcrelGatewayMeterIdCodec.format(device.getPortNo(), device.getMeterAddress());
        String transparent = gatewayTransparentCodec.encode(meterId, rtuFrame);

        byte[] encrypted = gatewayCryptoService.encrypt(transparent.getBytes(StandardCharsets.UTF_8), gateway.getDeviceSecret());
        byte[] frame = gatewayFrameCodec.encode(GatewayPacketCode.DOWNLINK, encrypted);

        CompletableFuture<byte[]> future = commandTransport.sendWithAck(gateway.getDeviceNo(), frame);
        ProtocolTimeoutSupport.applyTimeout(future, commandProperties.getTimeoutMillis(),
                ex -> commandTransport.failPending(gateway.getDeviceNo(), ex));
        return future.thenApply(payload -> translator.parseResponse(command, payload));
    }
}
