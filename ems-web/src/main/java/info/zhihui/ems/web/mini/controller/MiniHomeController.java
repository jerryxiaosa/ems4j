package info.zhihui.ems.web.mini.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import info.zhihui.ems.business.mini.utils.MiniStpUtil;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.mini.biz.MiniHomeBiz;
import info.zhihui.ems.web.mini.vo.MiniHomeSummaryVo;
import info.zhihui.ems.web.mini.vo.MiniHomeTrendVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序首页接口。
 */
@RestController
@RequestMapping(ApiPathConstant.V1 + "/mini/home")
@RequiredArgsConstructor
@Tag(name = "小程序首页接口")
public class MiniHomeController {

    private final MiniHomeBiz miniHomeBiz;

    @SaCheckLogin(type = MiniStpUtil.TYPE)
    @GetMapping("/summary")
    @Operation(summary = "查询首页摘要")
    public RestResult<MiniHomeSummaryVo> getSummary() {
        return ResultUtil.success(miniHomeBiz.getSummary());
    }

    @SaCheckLogin(type = MiniStpUtil.TYPE)
    @GetMapping("/trend")
    @Operation(summary = "查询首页近七日趋势")
    public RestResult<MiniHomeTrendVo> getTrend(@RequestParam String metric) {
        return ResultUtil.success(miniHomeBiz.getTrend(metric));
    }
}
