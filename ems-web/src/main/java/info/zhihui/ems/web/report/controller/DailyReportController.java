package info.zhihui.ems.web.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.report.biz.DailyReportBiz;
import info.zhihui.ems.web.report.vo.DailyReportBuildVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日报构建接口。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(ApiPathConstant.V1 + "/report/daily")
@Tag(name = "日报构建接口")
public class DailyReportController {

    private final DailyReportBiz dailyReportBiz;

    @SaCheckPermission("reports:daily:build")
    @PostMapping("/build")
    @Operation(summary = "手工构建日报")
    public RestResult<Void> buildDailyReport(@Valid @RequestBody DailyReportBuildVo buildVo) {
        dailyReportBiz.buildDailyReport(buildVo);
        return ResultUtil.success();
    }
}
