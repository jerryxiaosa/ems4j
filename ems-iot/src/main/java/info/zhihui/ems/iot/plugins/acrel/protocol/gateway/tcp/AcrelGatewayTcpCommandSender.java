package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.DeviceCommandSupport;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.MultiStepDeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.outbound.StepContext;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
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

    private static final int MULTI_STEP_MAX = 5;

    private final ProtocolCommandTransport commandTransport;
    private final DeviceCommandTranslatorResolver translatorRegistry;
    private final DeviceRegistry deviceRegistry;
    private final AcrelGatewayFrameCodec gatewayFrameCodec;
    private final AcrelGatewayCryptoService gatewayCryptoService;
    private final AcrelGatewayTransparentCodec gatewayTransparentCodec;

    /**
     * 网关命令执行上下文。
     */
    private static final class GatewayCommandContext {
        private final Device gateway;
        private final Device device;
        private final DeviceCommand command;
        private final DeviceCommandTranslator<ModbusRtuRequest> translator;

        private GatewayCommandContext(Device gateway,
                                      Device device,
                                      DeviceCommand command,
                                      DeviceCommandTranslator<ModbusRtuRequest> translator) {
            this.gateway = gateway;
            this.device = device;
            this.command = command;
            this.translator = translator;
        }

        /**
         * 获取多步命令翻译器。
         *
         * @return 多步翻译器
         */
        private MultiStepDeviceCommandTranslator<ModbusRtuRequest> asMultiStepTranslator() {
            return (MultiStepDeviceCommandTranslator<ModbusRtuRequest>) translator;
        }
    }

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
        GatewayCommandContext commandContext = new GatewayCommandContext(gateway, device, command, translator);
        if (translator instanceof MultiStepDeviceCommandTranslator<ModbusRtuRequest>) {
            StepContext stepContext = new StepContext();
            ModbusRtuRequest firstRequest = commandContext.asMultiStepTranslator().firstRequest(command);
            return sendStep(commandContext, stepContext, firstRequest, MULTI_STEP_MAX);
        }

        ModbusRtuRequest request = translator.toRequest(command);
        return sendOnce(commandContext, request);
    }

    /**
     * 发送单步命令并解析响应。
     */
    private CompletableFuture<DeviceCommandResult> sendOnce(GatewayCommandContext context, ModbusRtuRequest request) {
        byte[] frame = buildGatewayFrame(context.gateway, context.device, request);
        CompletableFuture<byte[]> future = commandTransport.sendWithAck(context.gateway.getDeviceNo(), frame);
        return future.thenApply(payload -> context.translator.parseResponse(context.command, payload));
    }

    /**
     * 发送多步命令的当前步骤并推进流程。
     */
    private CompletableFuture<DeviceCommandResult> sendStep(GatewayCommandContext commandContext,
                                                            StepContext stepContext,
                                                            ModbusRtuRequest request,
                                                            int remainingSteps) {
        if (remainingSteps <= 0) {
            return CompletableFuture.failedFuture(new IllegalStateException("多步命令超过最大步数"));
        }
        byte[] frame = buildGatewayFrame(commandContext.gateway, commandContext.device, request);
        CompletableFuture<byte[]> future = commandTransport.sendWithAck(commandContext.gateway.getDeviceNo(), frame);

        return future.thenCompose(payload -> {
            StepResult<ModbusRtuRequest> step = commandContext.asMultiStepTranslator()
                    .parseStep(commandContext.command, payload, stepContext);
            if (step == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("多步命令解析结果为空"));
            }
            if (step.isFinished()) {
                DeviceCommandResult result = step.getResult();
                if (result == null) {
                    return CompletableFuture.failedFuture(new IllegalStateException("多步命令返回结果为空"));
                }
                return CompletableFuture.completedFuture(result);
            }
            ModbusRtuRequest nextRequest = step.getNextRequest();
            if (nextRequest == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("多步命令超过最大步数或下一步为空"));
            }
            return sendStep(commandContext, stepContext, nextRequest, remainingSteps - 1);
        });
    }

    /**
     * 构建网关下行帧（RTU -> 透明 -> 加密 -> 网关帧）。
     */
    private byte[] buildGatewayFrame(Device gateway, Device device, ModbusRtuRequest rtuRequest) {
        log.debug("转换网关设备命令，deviceNo={}, rtuRequest={}", device.getDeviceNo(), rtuRequest);
        byte[] rtuFrame = ModbusRtuBuilder.build(rtuRequest);
        log.debug("转换成Modbus RTU帧，rtuFrame={}", HexUtils.toHexString(rtuFrame));

        byte[] transparent = gatewayTransparentCodec.encode(device.getPortNo(), device.getMeterAddress(), rtuFrame);
        log.debug("网关透明转发编码，transparent={}", new String(transparent, StandardCharsets.UTF_8));

        byte[] encrypted = gatewayCryptoService.encrypt(transparent, gateway.getDeviceSecret());
        byte[] frame = gatewayFrameCodec.encode(GatewayPacketCode.DOWNLINK, encrypted);
        log.debug("发送网关帧，frame={}", HexUtils.toHexString(frame));
        return frame;
    }

}
