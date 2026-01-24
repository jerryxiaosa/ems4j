package info.zhihui.ems.iot.plugins.acrel;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.port.registry.DeviceProtocolHandler;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.Acrel4gTcpInboundHandler;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.Acrel4gTcpCommandSender;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class Acrel4gProtocolHandler implements DeviceProtocolHandler {

    private final Acrel4gTcpInboundHandler tcpInboundHandler;
    private final Acrel4gTcpCommandSender commandSender;

    @Override
    public String getVendor() {
        return AcrelProtocolConstants.VENDOR;
    }

    @Override
    public DeviceAccessModeEnum getAccessMode() {
        return DeviceAccessModeEnum.DIRECT;
    }

    @Override
    public void onMessage(ProtocolMessageContext context) {
        if (TransportProtocolEnum.TCP.equals(context.getTransportType())) {
            tcpInboundHandler.handle(context);
            return;
        }
        throw new UnsupportedOperationException("不支持的传输类型" + context.getTransportType());
    }

    @Override
    public CompletableFuture<DeviceCommandResult> sendCommand(DeviceCommand command) {
        return commandSender.send(command);
    }

}
