package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.application.DeviceAppService;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
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
    private final DeviceVendorFacade deviceVendorFacade;

    @PostMapping
    public RestResult<Integer> addDevice(@Valid @RequestBody DeviceSaveVo body) {
        return ResultUtil.success(deviceAppService.addDevice(body));
    }

    @PutMapping("/{deviceId}")
    public RestResult<Void> updateDevice(@PathVariable Integer deviceId, @Valid @RequestBody DeviceSaveVo body) {
        deviceAppService.updateDevice(deviceId, body);
        return ResultUtil.success();
    }

    @DeleteMapping("/{deviceId}")
    public RestResult<Void> deleteDevice(@PathVariable Integer deviceId) {
        deviceAppService.deleteDevice(deviceId);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/online")
    public RestResult<Boolean> getOnline(@PathVariable Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getOnline(deviceId));
    }
}
