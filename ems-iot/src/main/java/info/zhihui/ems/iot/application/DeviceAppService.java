package info.zhihui.ems.iot.application;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.vo.DeviceSaveVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceAppService {

    private final DeviceRegistry deviceRegistry;

    public Integer addDevice(DeviceSaveVo body) {
        Device device = toDomain(null, body);
        return deviceRegistry.save(device);
    }

    public void updateDevice(Integer deviceId, DeviceSaveVo body) {
        Device device = toDomain(deviceId, body);
        deviceRegistry.update(device);
    }

    public void deleteDevice(Integer deviceId) {
        deviceRegistry.deleteById(deviceId);
    }

    private Device toDomain(Integer id, DeviceSaveVo body) {
        Product product = new Product()
                .setCode(body.getProductCode());
        return new Device()
                .setId(id)
                .setDeviceNo(body.getDeviceNo())
                .setPortNo(body.getPortNo())
                .setMeterAddress(body.getMeterAddress())
                .setDeviceSecret(body.getDeviceSecret())
                .setSlaveAddress(body.getSlaveAddress() == null ? 0 : body.getSlaveAddress())
                .setProduct(product)
                .setParentId(body.getParentId());
    }

}
