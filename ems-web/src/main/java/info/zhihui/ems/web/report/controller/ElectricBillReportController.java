package info.zhihui.ems.web.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.report.biz.ElectricBillReportBiz;
import info.zhihui.ems.web.report.vo.ElectricBillReportDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportPageVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportQueryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 电费报表查询接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(ApiPathConstant.V1 + "/report/electric-bill")
@Tag(name = "电费报表查询接口")
public class ElectricBillReportController {

    private final ElectricBillReportBiz electricBillReportBiz;

    @SaCheckPermission("reports:electric-bill:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询电费报表列表")
    public RestResult<PageResult<ElectricBillReportPageVo>> findPage(
            @Valid @NotNull @ModelAttribute ElectricBillReportQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(electricBillReportBiz.findPage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("reports:electric-bill:detail")
    @GetMapping("/{accountId}/detail")
    @Operation(summary = "查询电费报表详情")
    public RestResult<ElectricBillReportDetailVo> getDetail(
            @Parameter(description = "账户ID") @PathVariable Integer accountId,
            @Valid @NotNull @ModelAttribute ElectricBillReportQueryVo queryVo) {
        return ResultUtil.success(electricBillReportBiz.getDetail(accountId, queryVo));
    }
}
