package info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;

import java.util.Optional;

public final class DeviceCommandSupport {

    private DeviceCommandSupport() {
    }

    public static Device requireDevice(DeviceCommand command, DeviceAccessModeEnum expectedAccessMode) {
        Device device = command.getDevice();
        if (device == null) {
            throw new IllegalArgumentException("Device is null");
        }
        DeviceAccessModeEnum accessMode = Optional.ofNullable(device.getProduct())
                .map(Product::getAccessMode)
                .orElse(null);
        if (accessMode == null) {
            throw new IllegalArgumentException("设备接入方式缺失，deviceNo=" + device.getDeviceNo());
        }
        if (expectedAccessMode != null && !expectedAccessMode.equals(accessMode)) {
            throw new IllegalArgumentException("设备接入方式不匹配，deviceNo=" + device.getDeviceNo());
        }
        return device;
    }
}
