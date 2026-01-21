package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.device.biz.GatewayBiz;
import info.zhihui.ems.web.device.vo.*;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 网关管理接口
 */
@RestController
@RequestMapping("/device/gateways")
@Tag(name = "网关管理接口")
@Validated
@RequiredArgsConstructor
public class GatewayController {

    private final GatewayBiz gatewayBiz;

    @SaCheckPermission("devices:gateways:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询网关")
    public RestResult<PageResult<GatewayVo>> findGatewayPage(@Valid @ModelAttribute GatewayQueryVo queryVo,
                                                             @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
                                                             @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(gatewayBiz.findGatewayPage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("devices:gateways:list")
    @GetMapping
    @Operation(summary = "查询网关列表")
    public RestResult<List<GatewayVo>> findGatewayList(@Valid @ModelAttribute GatewayQueryVo queryVo) {
        return ResultUtil.success(gatewayBiz.findGatewayList(queryVo));
    }

    @SaCheckPermission("devices:gateways:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取网关详情")
    public RestResult<GatewayDetailVo> getGateway(@Parameter(description = "网关ID") @PathVariable Integer id) {
        return ResultUtil.success(gatewayBiz.getGateway(id));
    }

    @SaCheckPermission("devices:gateways:add")
    @PostMapping
    @Operation(summary = "新增网关")
    public RestResult<Integer> addGateway(@Valid @RequestBody GatewayAddVo addVo) {
        return ResultUtil.success(gatewayBiz.addGateway(addVo));
    }

    @SaCheckPermission("devices:gateways:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新网关")
    public RestResult<Void> updateGateway(@Parameter(description = "网关ID") @PathVariable Integer id,
                                          @Valid @RequestBody GatewayUpdateVo saveVo) {
        gatewayBiz.updateGateway(id, saveVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:gateways:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除网关")
    public RestResult<Void> deleteGateway(@Parameter(description = "网关ID") @PathVariable Integer id) {
        gatewayBiz.deleteGateway(id);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:gateways:online")
    @PutMapping("/online-status")
    @Operation(summary = "同步网关在线状态")
    public RestResult<Void> syncOnlineStatus(@Valid @RequestBody GatewayOnlineStatusVo onlineStatusVo) {
        gatewayBiz.syncOnlineStatus(onlineStatusVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("devices:gateways:communication")
    @GetMapping("/communication-options")
    @Operation(summary = "获取通信方式列表")
    public RestResult<List<String>> findCommunicationOptions() {
        return ResultUtil.success(gatewayBiz.findCommunicationOptions());
    }
}
