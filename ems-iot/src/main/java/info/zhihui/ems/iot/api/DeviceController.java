package info.zhihui.ems.iot.api;

import info.zhihui.ems.iot.application.DeviceAppService;
import info.zhihui.ems.iot.vo.DeviceSaveVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@Validated
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceAppService deviceAppService;

    @PostMapping
    public Integer addDevice(@Valid @RequestBody DeviceSaveVo body) {
        return deviceAppService.addDevice(body);
    }

    @PutMapping("/{deviceId}")
    public void updateDevice(@PathVariable Integer deviceId, @Valid @RequestBody DeviceSaveVo body) {
        deviceAppService.updateDevice(deviceId, body);
    }

    @DeleteMapping("/{deviceId}")
    public void deleteDevice(@PathVariable Integer deviceId) {
        deviceAppService.deleteDevice(deviceId);
    }
}
