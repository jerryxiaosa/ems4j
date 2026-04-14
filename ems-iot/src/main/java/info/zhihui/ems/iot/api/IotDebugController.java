package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.iot.application.IotDebugAppService;
import info.zhihui.ems.iot.vo.IotClientDetailVo;
import info.zhihui.ems.iot.vo.IotClientSimpleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debug/iot")
@Validated
@RequiredArgsConstructor
@Tag(name = "IoT调试接口")
public class IotDebugController {

    private final IotDebugAppService iotDebugAppService;

    @GetMapping("/clients")
    @Operation(summary = "查询在线IoT客户端列表")
    public RestResult<List<IotClientSimpleVo>> findClientList() {
        return ResultUtil.success(iotDebugAppService.findClientList());
    }

    @GetMapping("/clients/{deviceNo}")
    @Operation(summary = "按设备编号查询在线IoT客户端详情")
    public RestResult<IotClientDetailVo> getClientDetail(@PathVariable @NotBlank(message = "设备编号不能为空") String deviceNo) {
        return ResultUtil.success(iotDebugAppService.getClientDetail(deviceNo));
    }
}
