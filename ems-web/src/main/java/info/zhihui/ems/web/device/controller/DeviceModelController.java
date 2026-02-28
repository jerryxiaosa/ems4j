package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.device.biz.DeviceModelBiz;
import info.zhihui.ems.web.device.vo.DeviceModelQueryVo;
import info.zhihui.ems.web.device.vo.DeviceModelVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备型号接口。
 */
@RestController
@RequestMapping("/device/device-models")
@Tag(name = "设备型号接口")
@Validated
@RequiredArgsConstructor
public class DeviceModelController {

    private final DeviceModelBiz deviceModelBiz;

    @SaCheckPermission("devices:models:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询设备型号")
    public RestResult<PageResult<DeviceModelVo>> findDeviceModelPage(@Valid @ModelAttribute DeviceModelQueryVo queryVo,
                                                                     @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
                                                                     @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(deviceModelBiz.findDeviceModelPage(queryVo, pageNum, pageSize));
    }
}
