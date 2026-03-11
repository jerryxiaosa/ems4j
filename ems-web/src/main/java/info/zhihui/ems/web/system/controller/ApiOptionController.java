package info.zhihui.ems.web.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.system.biz.ApiOptionBiz;
import info.zhihui.ems.web.system.vo.ApiOptionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统接口选项接口
 */
@RestController
@RequestMapping(ApiPathConstant.V1 + "/system")
@Validated
@RequiredArgsConstructor
@Tag(name = "系统接口选项接口")
public class ApiOptionController {

    private final ApiOptionBiz apiOptionBiz;

    @SaCheckPermission("system:api:list")
    @GetMapping("/api-options")
    @Operation(summary = "查询系统接口选项")
    public RestResult<List<ApiOptionVo>> findApiOptionList() {
        return ResultUtil.success(apiOptionBiz.findApiOptionList());
    }
}
