package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.device.biz.DeviceTypeBiz;
import info.zhihui.ems.web.device.vo.DeviceTypeTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备品类接口。
 */
@RestController
@RequestMapping("/device/device-types")
@Tag(name = "设备品类接口")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeBiz deviceTypeBiz;

    @SaCheckPermission("devices:types:tree")
    @GetMapping("/tree")
    @Operation(summary = "查询设备品类树")
    public RestResult<List<DeviceTypeTreeVo>> findDeviceTypeTree() {
        return ResultUtil.success(deviceTypeBiz.findDeviceTypeTree());
    }
}
