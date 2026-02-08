package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.application.DeviceAppService;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
import info.zhihui.ems.iot.vo.DeviceSaveVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@Validated
@RequiredArgsConstructor
@Tag(name = "设备管理接口")
public class DeviceController {

    private final DeviceAppService deviceAppService;
    private final DeviceVendorFacade deviceVendorFacade;

    @PostMapping
    @Operation(summary = "新增设备")
    public RestResult<Integer> addDevice(@Valid @RequestBody DeviceSaveVo body) {
        return ResultUtil.success(deviceAppService.addDevice(body));
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备")
    public RestResult<Void> updateDevice(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                         @Valid @RequestBody DeviceSaveVo body) {
        deviceAppService.updateDevice(deviceId, body);
        return ResultUtil.success();
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备")
    public RestResult<Void> deleteDevice(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        deviceAppService.deleteDevice(deviceId);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/online")
    @Operation(summary = "查询设备在线状态")
    public RestResult<Boolean> getOnline(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getOnline(deviceId));
    }
}
