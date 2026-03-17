package info.zhihui.ems.web.device.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.device.biz.DeviceOperationBiz;
import info.zhihui.ems.web.device.vo.DeviceOperationDetailVo;
import info.zhihui.ems.web.device.vo.DeviceOperationExecuteRecordVo;
import info.zhihui.ems.web.device.vo.DeviceOperationQueryVo;
import info.zhihui.ems.web.device.vo.DeviceOperationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备操作接口
 */
@RestController
@RequestMapping(ApiPathConstant.V1 + "/device/operations")
@Tag(name = "设备操作接口")
@Validated
@RequiredArgsConstructor
public class DeviceOperationController {

    private final DeviceOperationBiz deviceOperationBiz;

    @SaCheckPermission("devices:operations:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询设备操作")
    public RestResult<PageResult<DeviceOperationVo>> findDeviceOperationPage(
            @ModelAttribute DeviceOperationQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultUtil.success(deviceOperationBiz.findDeviceOperationPage(queryVo, pageNum, pageSize));
    }

    @SaCheckPermission("devices:operations:detail")
    @GetMapping("/{id}")
    @Operation(summary = "查询设备操作详情")
    public RestResult<DeviceOperationDetailVo> getDeviceOperation(@PathVariable Integer id) {
        return ResultUtil.success(deviceOperationBiz.getDeviceOperation(id));
    }

    @SaCheckPermission("devices:operations:detail")
    @GetMapping("/{id}/execute-records")
    @Operation(summary = "查询设备操作执行记录")
    public RestResult<List<DeviceOperationExecuteRecordVo>> findDeviceOperationExecuteRecordList(@PathVariable Integer id) {
        return ResultUtil.success(deviceOperationBiz.findDeviceOperationExecuteRecordList(id));
    }


    @SaCheckPermission("devices:operations:detail")
    @PostMapping("/{id}/retry")
    @Operation(summary = "重试设备操作")
    public RestResult<Void> retryDeviceOperation(@PathVariable Integer id) {
        deviceOperationBiz.retryDeviceOperation(id);
        return ResultUtil.success();
    }
}
