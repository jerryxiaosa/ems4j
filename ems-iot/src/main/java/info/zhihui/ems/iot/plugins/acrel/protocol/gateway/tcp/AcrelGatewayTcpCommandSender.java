package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.config.IotCommandProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.DeviceCommandSupport;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.util.ProtocolTimeoutSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 安科瑞网关 TCP 下行命令发送器。
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
        log.debug("转换网关设备命令，deviceNo={}, rtuRequest={}", device.getDeviceNo(), rtuRequest);

        byte[] rtuFrame = ModbusRtuBuilder.build(rtuRequest);
        log.debug("转换成Modbus RTU帧，rtuFrame={}", HexUtils.toHexString(rtuFrame));

        byte[] transparent = gatewayTransparentCodec.encode(device.getPortNo(), device.getMeterAddress(), rtuFrame);
        log.debug("网关透明转发编码，transparent={}", new String(transparent, StandardCharsets.UTF_8));

        byte[] encrypted = gatewayCryptoService.encrypt(transparent, gateway.getDeviceSecret());
        byte[] frame = gatewayFrameCodec.encode(GatewayPacketCode.DOWNLINK, encrypted);
        log.debug("发送网关帧，frame={}", HexUtils.toHexString(frame));

        CompletableFuture<byte[]> future = commandTransport.sendWithAck(gateway.getDeviceNo(), frame);
        ProtocolTimeoutSupport.applyTimeout(future, commandProperties.getTimeoutMillis(),
                ex -> commandTransport.failPending(gateway.getDeviceNo(), ex));
        return future.thenApply(payload -> translator.parseResponse(command, payload));
    }

}
