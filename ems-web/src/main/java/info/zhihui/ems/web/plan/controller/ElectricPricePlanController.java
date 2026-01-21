package info.zhihui.ems.web.plan.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.plan.biz.ElectricPricePlanBiz;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanDetailVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanQueryVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanSaveVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTimeSettingVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTypeVo;
import info.zhihui.ems.web.plan.vo.StepPriceVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 电价方案接口
 */
@RestController
@RequestMapping("/plan/electric-price-plans")
@Tag(name = "电价方案管理接口")
@Validated
@RequiredArgsConstructor
public class ElectricPricePlanController {

    private final ElectricPricePlanBiz electricPricePlanBiz;

    @SaCheckPermission("plans:electric-price:list")
    @GetMapping
    @Operation(summary = "查询电价方案列表")
    public RestResult<List<ElectricPricePlanVo>> findElectricPricePlanList(@Valid @ModelAttribute ElectricPricePlanQueryVo queryVo) {
        return ResultUtil.success(electricPricePlanBiz.findElectricPricePlanList(queryVo));
    }

    @SaCheckPermission("plans:electric-price:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取电价方案详情")
    public RestResult<ElectricPricePlanDetailVo> getElectricPricePlan(@Parameter(description = "方案ID") @PathVariable Integer id) {
        return ResultUtil.success(electricPricePlanBiz.getElectricPricePlan(id));
    }

    @SaCheckPermission("plans:electric-price:add")
    @PostMapping
    @Operation(summary = "新增电价方案")
    public RestResult<Integer> addElectricPricePlan(@Valid @RequestBody ElectricPricePlanSaveVo saveVo) {
        return ResultUtil.success(electricPricePlanBiz.addElectricPricePlan(saveVo));
    }

    @SaCheckPermission("plans:electric-price:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新电价方案")
    public RestResult<Void> updateElectricPricePlan(@Parameter(description = "方案ID") @PathVariable Integer id,
                                                    @Valid @RequestBody ElectricPricePlanSaveVo saveVo) {
        electricPricePlanBiz.updateElectricPricePlan(id, saveVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("plans:electric-price:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除电价方案")
    public RestResult<Void> deleteElectricPricePlan(@Parameter(description = "方案ID") @PathVariable Integer id) {
        electricPricePlanBiz.deleteElectricPricePlan(id);
        return ResultUtil.success();
    }

    @SaCheckPermission("plans:electric-price:step:get")
    @GetMapping("/default/step-price")
    @Operation(summary = "获取默认阶梯电价配置")
    public RestResult<List<StepPriceVo>> getDefaultStepPrice() {
        return ResultUtil.success(electricPricePlanBiz.getDefaultStepPrice());
    }

    @SaCheckPermission("plans:electric-price:step:update")
    @PutMapping("/default/step-price")
    @Operation(summary = "更新默认阶梯电价配置")
    public RestResult<Void> updateDefaultStepPrice(@Valid @RequestBody List<StepPriceVo> stepPriceVos) {
        electricPricePlanBiz.updateDefaultStepPrice(stepPriceVos);
        return ResultUtil.success();
    }

    @SaCheckPermission("plans:electric-price:time:get")
    @GetMapping("/default/time")
    @Operation(summary = "获取默认尖峰平谷时间段")
    public RestResult<List<ElectricPriceTimeSettingVo>> getDefaultElectricTime() {
        return ResultUtil.success(electricPricePlanBiz.getDefaultElectricTime());
    }

    @SaCheckPermission("plans:electric-price:time:update")
    @PutMapping("/default/time")
    @Operation(summary = "更新默认尖峰平谷时间段")
    public RestResult<Void> updateDefaultElectricTime(@Valid @RequestBody List<ElectricPriceTimeSettingVo> timeList) {
        electricPricePlanBiz.updateDefaultElectricTime(timeList);
        return ResultUtil.success();
    }

    @SaCheckPermission("plans:electric-price:price:get")
    @GetMapping("/default/price")
    @Operation(summary = "获取默认尖峰平谷电价")
    public RestResult<List<ElectricPriceTypeVo>> getDefaultElectricPrice() {
        return ResultUtil.success(electricPricePlanBiz.getDefaultElectricPrice());
    }

    @SaCheckPermission("plans:electric-price:price:update")
    @PutMapping("/default/price")
    @Operation(summary = "更新默认尖峰平谷电价")
    public RestResult<Void> updateDefaultElectricPrice(@Valid @RequestBody List<ElectricPriceTypeVo> priceList) {
        electricPricePlanBiz.updateDefaultElectricPrice(priceList);
        return ResultUtil.success();
    }
}
