package info.zhihui.ems.web.plan.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.plan.biz.WarnPlanBiz;
import info.zhihui.ems.web.plan.vo.WarnPlanQueryVo;
import info.zhihui.ems.web.plan.vo.WarnPlanSaveVo;
import info.zhihui.ems.web.plan.vo.WarnPlanVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预警方案接口
 */
@RestController
@RequestMapping("/plan/warn-plans")
@Tag(name = "预警方案管理接口")
@Validated
@RequiredArgsConstructor
public class WarnPlanController {

    private final WarnPlanBiz warnPlanBiz;

    @SaCheckPermission("plans:warn:list")
    @GetMapping
    @Operation(summary = "查询预警方案列表")
    public RestResult<List<WarnPlanVo>> findWarnPlanList(@Valid @ModelAttribute WarnPlanQueryVo queryVo) {
        return ResultUtil.success(warnPlanBiz.findWarnPlanList(queryVo));
    }

    @SaCheckPermission("plans:warn:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取预警方案详情")
    public RestResult<WarnPlanVo> getWarnPlan(@Parameter(description = "方案ID") @PathVariable Integer id) {
        return ResultUtil.success(warnPlanBiz.getWarnPlan(id));
    }

    @SaCheckPermission("plans:warn:add")
    @PostMapping
    @Operation(summary = "新增预警方案")
    public RestResult<Integer> addWarnPlan(@Valid @RequestBody WarnPlanSaveVo saveVo) {
        return ResultUtil.success(warnPlanBiz.addWarnPlan(saveVo));
    }

    @SaCheckPermission("plans:warn:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新预警方案")
    public RestResult<Void> updateWarnPlan(@Parameter(description = "方案ID") @PathVariable Integer id,
                                           @Valid @RequestBody WarnPlanSaveVo saveVo) {
        warnPlanBiz.updateWarnPlan(id, saveVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("plans:warn:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警方案")
    public RestResult<Void> deleteWarnPlan(@Parameter(description = "方案ID") @PathVariable Integer id) {
        warnPlanBiz.deleteWarnPlan(id);
        return ResultUtil.success();
    }
}
