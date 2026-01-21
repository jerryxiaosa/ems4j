package info.zhihui.ems.web.order.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.order.biz.ServiceRateBiz;
import info.zhihui.ems.web.order.vo.ServiceRateVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 服务费率接口
 */
@RestController
@RequestMapping("/orders/service-rate")
@Tag(name = "服务费率接口")
@Validated
@RequiredArgsConstructor
public class ServiceRateController {

    private final ServiceRateBiz serviceRateBiz;

    @SaCheckPermission("orders:service-rate:get")
    @GetMapping("/default")
    @Operation(summary = "获取默认服务费率")
    public RestResult<ServiceRateVo> getDefaultServiceRate() {
        BigDecimal rate = serviceRateBiz.getDefaultServiceRate();
        return ResultUtil.success(new ServiceRateVo().setDefaultServiceRate(rate));
    }

    @SaCheckPermission("orders:service-rate:update")
    @PutMapping("/default")
    @Operation(summary = "更新默认服务费率")
    public RestResult<Void> updateDefaultServiceRate(@Valid @RequestBody ServiceRateVo serviceRateVo) {
        serviceRateBiz.updateDefaultServiceRate(serviceRateVo.getDefaultServiceRate());
        return ResultUtil.success();
    }
}
