package info.zhihui.ems.iot.simulator.runtime;

import java.time.LocalDateTime;

/**
 * 设备运行上下文更新器。
 */
public final class SimulatorDeviceContextUpdater {

    private SimulatorDeviceContextUpdater() {
    }

    public static void markConnected(SimulatorDeviceContext deviceContext, String connectionId, LocalDateTime connectTime) {
        if (deviceContext == null) {
            return;
        }
        deviceContext.setConnectionId(connectionId);
        deviceContext.setConnectedAt(connectTime);
        deviceContext.setLastCommunicationAt(connectTime);
    }

    public static void markDisconnected(SimulatorDeviceContext deviceContext) {
        if (deviceContext == null) {
            return;
        }
        deviceContext.setConnectionId(null);
        deviceContext.setConnectedAt(null);
    }

    public static void touch(SimulatorDeviceContext deviceContext, LocalDateTime communicationTime) {
        if (deviceContext == null) {
            return;
        }
        deviceContext.setLastCommunicationAt(communicationTime);
    }
}
