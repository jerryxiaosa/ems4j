package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.device.biz.ElectricMeterBiz;
import info.zhihui.ems.web.device.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电表管理接口
 */
@RestController
@RequestMapping("/device/electric-meters")
@Tag(name = "电表管理接口")
@Validated
@RequiredArgsConstructor
public class ElectricMeterController {

    private final ElectricMeterBiz electricMeterBiz;

    @SaCheckPermission("devices:meters:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询电表")
    public RestResult<PageResult<ElectricMeterVo>> findElectricMeterPage(@Valid @ModelAttribute ElectricMeterQueryVo queryVo,
                                                                         @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
                                                                         @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ElectricMeterVo> page = electricMeterBiz.findElectricMeterPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("devices:meters:list")
    @GetMapping
    @Operation(summary = "查询电表列表")
    public RestResult<List<ElectricMeterVo>> findElectricMeterList(@Valid @ModelAttribute ElectricMeterQueryVo queryVo) {
        return ResultUtil.success(electricMeterBiz.findElectricMeterList(queryVo));
    }

    @SaCheckPermission("devices:meters:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取电表详情")
    public RestResult<ElectricMeterDetailVo> getElectricMeter(@Parameter(description = "电表ID") @PathVariable Integer id) {
        return ResultUtil.success(electricMeterBiz.getElectricMeter(id));
    }

    @SaCheckPermission("devices:meters:detail")
    @GetMapping("/{id}/latest-power-record")
    @Operation(summary = "获取电表最近一次上报电量记录")
    public RestResult<ElectricMeterLatestPowerRecordVo> getLatestPowerRecord(
            @Parameter(description = "电表ID") @PathVariable Integer id) {
        return ResultUtil.success(electricMeterBiz.getLatestPowerRecord(id));
    }

    @SaCheckPermission("devices:meters:add")
    @PostMapping
    @Operation(summary = "新增电表")
    public RestResult<Integer> addElectricMeter(@Valid @RequestBody ElectricMeterCreateVo createVo) {
        return ResultUtil.success(electricMeterBiz.addElectricMeter(createVo));
    }

    @SaCheckPermission("devices:meters:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新电表")
    public RestResult<Void> updateElectricMeter(@Parameter(description = "电表ID") @PathVariable Integer id,
                                                @Valid @RequestBody ElectricMeterUpdateVo updateVo) {
        electricMeterBiz.updateElectricMeter(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除电表")
    public RestResult<Void> deleteElectricMeter(@Parameter(description = "电表ID") @PathVariable Integer id) {
        electricMeterBiz.deleteElectricMeter(id);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:switch")
    @PutMapping("/switch")
    @Operation(summary = "设置电表开关状态")
    public RestResult<Void> changeSwitchStatus(@Valid @RequestBody ElectricMeterSwitchStatusVo switchStatusVo) {
        electricMeterBiz.changeSwitchStatus(switchStatusVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:time")
    @PutMapping("/time")
    @Operation(summary = "设置电价时间段")
    public RestResult<Void> updateElectricTime(@Valid @RequestBody ElectricMeterTimeVo timeVo) {
        electricMeterBiz.updateElectricTime(timeVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:protect")
    @PutMapping("/protect")
    @Operation(summary = "设置保电模式")
    public RestResult<Void> updateProtectModel(@Valid @RequestBody ElectricMeterProtectVo protectVo) {
        electricMeterBiz.updateProtectModel(protectVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:ct")
    @PutMapping("/ct")
    @Operation(summary = "设置电表CT变比")
    public RestResult<Integer> updateMeterCt(@Valid @RequestBody ElectricMeterCtVo ctVo) {
        return ResultUtil.success(electricMeterBiz.updateMeterCt(ctVo));
    }

    @SaCheckPermission("devices:meters:online")
    @PutMapping("/online-status")
    @Operation(summary = "同步电表在线状态")
    public RestResult<Void> syncOnlineStatus(@Valid @RequestBody ElectricMeterOnlineStatusVo onlineStatusVo) {
        electricMeterBiz.syncOnlineStatus(onlineStatusVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:meters:power")
    @PostMapping("/{id}/power")
    @Operation(summary = "查询电表实时电量")
    public RestResult<List<ElectricMeterPowerVo>> getMeterPower(@Parameter(description = "电表ID") @PathVariable Integer id,
                                                                @Valid @RequestBody ElectricMeterPowerQueryVo queryVo) {
        return ResultUtil.success(electricMeterBiz.getMeterPower(id, queryVo));
    }

}
