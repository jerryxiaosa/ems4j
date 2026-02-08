package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
import info.zhihui.ems.iot.vo.electric.ElectricDateDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationUpdateVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/commands")
@Validated
@RequiredArgsConstructor
@Tag(name = "设备命令接口")
public class CommandController {

    private final DeviceVendorFacade deviceVendorFacade;

    @PostMapping("/{deviceId}/cut-off")
    @Operation(summary = "下发拉闸命令")
    public RestResult<Void> cutOff(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        deviceVendorFacade.cutPower(deviceId);
        return ResultUtil.success();
    }

    @PostMapping("/{deviceId}/recover")
    @Operation(summary = "下发合闸命令")
    public RestResult<Void> recover(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        deviceVendorFacade.recoverPower(deviceId);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/ct")
    @Operation(summary = "读取CT倍率")
    public RestResult<Integer> getCt(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getCt(deviceId));
    }

    @PostMapping("/{deviceId}/ct")
    @Operation(summary = "下发CT倍率")
    public RestResult<Void> setCt(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                  @RequestParam @NotNull(message = "互感器倍率不能为空") @Positive(message = "互感器倍率必须大于0") Integer ct) {
        deviceVendorFacade.setCt(deviceId, ct);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/duration")
    @Operation(summary = "读取日时段电价方案")
    public RestResult<List<ElectricDurationVo>> getDuration(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                                            @RequestParam @NotNull(message = "日方案编号不能为空")
                                                            @Min(value = 1, message = "日方案编号范围为1~2")
                                                            @Max(value = 2, message = "日方案编号范围为1~2") Integer dailyPlanId) {
        return ResultUtil.success(deviceVendorFacade.getDuration(deviceId, dailyPlanId));
    }

    @PostMapping("/{deviceId}/duration")
    @Operation(summary = "下发日时段电价方案")
    public RestResult<Void> setDuration(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                        @Valid @RequestBody ElectricDurationUpdateVo dto) {
        deviceVendorFacade.setDuration(deviceId, dto);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/date-duration")
    @Operation(summary = "读取指定日期电价方案")
    public RestResult<List<ElectricDateDurationVo>> getDateDuration(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getDateDuration(deviceId));
    }

    @PostMapping("/{deviceId}/date-duration")
    @Operation(summary = "下发指定日期电价方案")
    public RestResult<Void> setDateDuration(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                            @Valid @NotEmpty @RequestBody List<ElectricDateDurationVo> dto) {
        deviceVendorFacade.setDateDuration(deviceId, dto);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/used-power")
    @Operation(summary = "读取分时电量")
    public RestResult<BigDecimal> getUsedPower(@PathVariable @NotNull(message = "设备ID不能为空") @Positive(message = "设备ID必须大于0") Integer deviceId,
                                               @RequestParam(required = false) @Min(value = 0, message = "电量类型范围为0~5")
                                               @Max(value = 5, message = "电量类型范围为0~5") Integer type) {
        return ResultUtil.success(deviceVendorFacade.getUsedPower(deviceId, type));
    }
}
