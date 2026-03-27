package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContextUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 安科瑞 4G 模拟客户端最小门面，仅负责基础报文构造。
 */
@Component
@RequiredArgsConstructor
public class Acrel4gSimulatorClient {

    private final Acrel4gMessageFactory messageFactory;

    public byte[] buildRegisterFrame(SimulatorDeviceContext deviceContext, RegisterMessage registerMessage) {
        touch(deviceContext);
        return messageFactory.buildRegisterFrame(registerMessage);
    }

    public byte[] buildHeartbeatFrame(SimulatorDeviceContext deviceContext, HeartbeatMessage heartbeatMessage) {
        touch(deviceContext);
        return messageFactory.buildHeartbeatFrame(heartbeatMessage);
    }

    public byte[] buildDataUploadFrame(SimulatorDeviceContext deviceContext, DataUploadMessage dataUploadMessage) {
        touch(deviceContext);
        return messageFactory.buildDataUploadFrame(dataUploadMessage);
    }

    private void touch(SimulatorDeviceContext deviceContext) {
        if (deviceContext != null) {
            SimulatorDeviceContextUpdater.touch(deviceContext, LocalDateTime.now());
        }
    }
}
