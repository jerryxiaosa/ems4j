package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp;

import info.zhihui.ems.iot.config.IotCommandProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.plugins.acrel.support.DeviceCommandSupport;
import info.zhihui.ems.iot.util.ProtocolTimeoutSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 4G TCP 下行命令发送器。
 */
@Component
@RequiredArgsConstructor
public class Acrel4gTcpCommandSender {

    private final ProtocolCommandTransport commandTransport;
    private final DeviceCommandTranslatorResolver translatorRegistry;
    private final IotCommandProperties commandProperties;
    private final Acrel4gFrameCodec frameCodec;

    public CompletableFuture<DeviceCommandResult> send(DeviceCommand command) {
        Device device = DeviceCommandSupport.requireDevice(command, DeviceAccessModeEnum.DIRECT);

        DeviceCommandTranslator<ModbusRtuRequest> translator = translatorRegistry.resolve(
                device.getProduct().getVendor(), device.getProduct().getCode(), command.getType(), ModbusRtuRequest.class);
        ModbusRtuRequest rtuRequest = translator.toRequest(command);
        // 组装 Modbus RTU 帧用于下行请求。
        byte[] rtuFrame = ModbusRtuBuilder.build(rtuRequest);
        // 将 RTU 帧封装为 4G 协议下行报文。
        byte[] frame = frameCodec.encode(Acrel4gPacketCode.DOWNLINK, rtuFrame);

        CompletableFuture<byte[]> future = commandTransport.sendWithAck(device.getDeviceNo(), frame);
        ProtocolTimeoutSupport.applyTimeout(future, commandProperties.getTimeoutMillis(),
                ex -> commandTransport.failPending(device.getDeviceNo(), ex));
        return future.thenApply(payload -> translator.parseResponse(command, payload));
    }
}
