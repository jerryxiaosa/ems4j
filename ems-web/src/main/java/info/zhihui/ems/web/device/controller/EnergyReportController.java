package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.device.biz.EnergyReportBiz;
import info.zhihui.ems.web.device.vo.StandardEnergyReportSaveVo;
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
 * 电量上报接口
 */
@RestController
@RequestMapping("/device/energy-reports")
@Tag(name = "电量上报接口")
@Validated
@RequiredArgsConstructor
public class EnergyReportController {

    private final EnergyReportBiz energyReportBiz;

    @SaIgnore
    @PostMapping("/standard")
    @Operation(summary = "标准电量上报")
    public RestResult<Void> addStandardReport(@Valid @RequestBody StandardEnergyReportSaveVo saveVo) {
        energyReportBiz.addStandardReport(saveVo);
        return ResultUtil.success();
    }
}
