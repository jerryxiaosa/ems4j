package info.zhihui.ems.web.finance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.finance.biz.FinanceBiz;
import info.zhihui.ems.web.finance.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 财务消费相关接口
 */
@RestController
@RequestMapping("/finance")
@Tag(name = "财务消费接口")
@Validated
@RequiredArgsConstructor
public class FinanceConsumeController {

    private final FinanceBiz financeBiz;

    @SaCheckPermission("finance:account-consume:page")
    @GetMapping("/account-consumes")
    @Operation(summary = "分页查询账户（包月）消费记录")
    public RestResult<PageResult<AccountConsumeRecordVo>> findAccountConsumePage(
            @Valid @ModelAttribute AccountConsumeQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(financeBiz.findAccountConsumePage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("finance:meter-consume:page")
    @GetMapping("/meter-consumes")
    @Operation(summary = "分页查询电量消费记录")
    public RestResult<PageResult<PowerConsumeRecordVo>> findPowerConsumePage(
            @Valid @ModelAttribute PowerConsumeQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(financeBiz.findPowerConsumePage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("finance:meter-correction:page")
    @GetMapping("/meter-corrections")
    @Operation(summary = "分页查询补正记录")
    public RestResult<PageResult<CorrectionRecordVo>> findCorrectionRecordPage(
            @Valid @ModelAttribute CorrectionRecordQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(financeBiz.findCorrectionRecordPage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("finance:meter-correction:add")
    @PostMapping("/meter-corrections")
    @Operation(summary = "新增补正记录")
    public RestResult<Void> correctByAmount(@Valid @RequestBody CorrectionMeterAmountVo correctionMeterAmountVo) {
        financeBiz.correctByAmount(correctionMeterAmountVo);
        return ResultUtil.success();
    }
}
