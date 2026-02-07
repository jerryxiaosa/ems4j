package info.zhihui.ems.web.order.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.order.biz.OrderBiz;
import info.zhihui.ems.web.order.vo.EnergyOrderCreateVo;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import info.zhihui.ems.web.order.vo.OrderDetailVo;
import info.zhihui.ems.web.order.vo.OrderQueryVo;
import info.zhihui.ems.web.order.vo.OrderVo;
import info.zhihui.ems.web.order.vo.TerminationOrderCreateVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "订单管理接口")
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderBiz orderBiz;

    @SaCheckPermission("orders:orders:list")
    @GetMapping
    @Operation(summary = "分页查询订单列表")
    public RestResult<PageResult<OrderVo>> findOrdersPage(
            @Valid @ModelAttribute OrderQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(orderBiz.findOrdersPage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("orders:orders:add-energy")
    @PostMapping("/energy-top-up")
    @Operation(summary = "创建能耗充值订单")
    public RestResult<OrderCreationResponseVo> createEnergyTopUpOrder(@Valid @RequestBody EnergyOrderCreateVo createVo) {
        return ResultUtil.success(orderBiz.createEnergyTopUpOrder(createVo));
    }

    @SaCheckPermission("orders:orders:add-termination")
    @PostMapping("/termination")
    @Operation(summary = "创建销户结算订单")
    public RestResult<OrderCreationResponseVo> createTerminationOrder(@Valid @RequestBody TerminationOrderCreateVo createVo) {
        return ResultUtil.success(orderBiz.createTerminationOrder(createVo));
    }

    @SaCheckPermission("orders:orders:detail")
    @GetMapping("/{orderSn}")
    @Operation(summary = "获取订单详情")
    public RestResult<OrderDetailVo> getOrderDetail(@Parameter(description = "订单编号") @PathVariable String orderSn) {
        return ResultUtil.success(orderBiz.getOrderDetail(orderSn));
    }

    @SaCheckPermission("orders:orders:close")
    @PostMapping("/{orderSn}/close")
    @Operation(summary = "关闭订单")
    public RestResult<Void> closeOrder(@Parameter(description = "订单编号") @PathVariable String orderSn) {
        orderBiz.closeOrder(orderSn);
        return ResultUtil.success();
    }

    @SaIgnore
    @PostMapping("/weixin/pay-notify")
    @Operation(summary = "处理微信支付通知")
    public void answerWeiXinPayNotify(HttpServletRequest request) {
        orderBiz.answerWeiXinPayNotify(request);
    }
}
