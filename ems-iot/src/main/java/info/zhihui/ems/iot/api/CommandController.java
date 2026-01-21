package info.zhihui.ems.iot.api;

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
    public void cutOff(@PathVariable Integer deviceId) {
        deviceVendorFacade.cutPower(deviceId);
    }

    @PostMapping("/{deviceId}/recover")
    public void recover(@PathVariable Integer deviceId) {
        deviceVendorFacade.recoverPower(deviceId);
    }

    @GetMapping("/{deviceId}/ct")
    public Integer getCt(@PathVariable Integer deviceId) {
        return deviceVendorFacade.getCt(deviceId);
    }

    @PostMapping("/{deviceId}/ct")
    public void setCt(@PathVariable Integer deviceId, @RequestParam Integer ct) {
        deviceVendorFacade.setCt(deviceId, ct);
    }

    @GetMapping("/{deviceId}/duration")
    public List<ElectricDurationVo> getDuration(@PathVariable Integer deviceId,
                                                 @RequestParam Integer plan) {
        return deviceVendorFacade.getDuration(deviceId, plan);
    }

    @PostMapping("/{deviceId}/duration")
    public void setDuration(@PathVariable Integer deviceId,
                            @Valid @RequestBody ElectricDurationUpdateVo dto) {
        deviceVendorFacade.setDuration(deviceId, dto);
    }

    @GetMapping("/{deviceId}/date-duration/{plan}")
    public List<ElectricDateDurationVo> getDateDuration(@PathVariable Integer deviceId,
                                                         @PathVariable String plan) {
        return deviceVendorFacade.getDateDuration(deviceId, plan);
    }

    @PostMapping("/{deviceId}/date-duration/{plan}")
    public void setDateDuration(@PathVariable Integer deviceId,
                                @PathVariable String plan,
                                @Valid @NotEmpty @RequestBody List<ElectricDateDurationVo> dto) {
        deviceVendorFacade.setDateDuration(deviceId, plan, dto);
    }

    @GetMapping("/{deviceId}/used-power")
    public BigDecimal getUsedPower(@PathVariable Integer deviceId,
                                   @RequestParam(required = false) Integer type) {
        return deviceVendorFacade.getUsedPower(deviceId, type);
    }
}
