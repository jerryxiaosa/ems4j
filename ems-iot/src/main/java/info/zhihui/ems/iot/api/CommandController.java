package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
import info.zhihui.ems.iot.vo.electric.ElectricDateDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationUpdateVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/commands")
@Validated
@RequiredArgsConstructor
public class CommandController {

    private final DeviceVendorFacade deviceVendorFacade;

    @PostMapping("/{deviceId}/cut-off")
    public RestResult<Void> cutOff(@PathVariable Integer deviceId) {
        deviceVendorFacade.cutPower(deviceId);
        return ResultUtil.success();
    }

    @PostMapping("/{deviceId}/recover")
    public RestResult<Void> recover(@PathVariable Integer deviceId) {
        deviceVendorFacade.recoverPower(deviceId);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/ct")
    public RestResult<Integer> getCt(@PathVariable Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getCt(deviceId));
    }

    @PostMapping("/{deviceId}/ct")
    public RestResult<Void> setCt(@PathVariable Integer deviceId, @RequestParam Integer ct) {
        deviceVendorFacade.setCt(deviceId, ct);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/duration")
    public RestResult<List<ElectricDurationVo>> getDuration(@PathVariable Integer deviceId,
                                                            @RequestParam Integer dailyPlanId) {
        return ResultUtil.success(deviceVendorFacade.getDuration(deviceId, dailyPlanId));
    }

    @PostMapping("/{deviceId}/duration")
    public RestResult<Void> setDuration(@PathVariable Integer deviceId,
                                        @Valid @RequestBody ElectricDurationUpdateVo dto) {
        deviceVendorFacade.setDuration(deviceId, dto);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/date-duration")
    public RestResult<List<ElectricDateDurationVo>> getDateDuration(@PathVariable Integer deviceId) {
        return ResultUtil.success(deviceVendorFacade.getDateDuration(deviceId));
    }

    @PostMapping("/{deviceId}/date-duration")
    public RestResult<Void> setDateDuration(@PathVariable Integer deviceId,
                                            @Valid @NotEmpty @RequestBody List<ElectricDateDurationVo> dto) {
        deviceVendorFacade.setDateDuration(deviceId, dto);
        return ResultUtil.success();
    }

    @GetMapping("/{deviceId}/used-power")
    public RestResult<BigDecimal> getUsedPower(@PathVariable Integer deviceId,
                                               @RequestParam(required = false) Integer type) {
        return ResultUtil.success(deviceVendorFacade.getUsedPower(deviceId, type));
    }
}
