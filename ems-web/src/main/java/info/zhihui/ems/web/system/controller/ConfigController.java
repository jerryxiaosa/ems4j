package info.zhihui.ems.web.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.system.biz.ConfigBiz;
import info.zhihui.ems.web.system.vo.ConfigQueryVo;
import info.zhihui.ems.web.system.vo.ConfigUpdateVo;
import info.zhihui.ems.web.system.vo.ConfigVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置管理接口
 */
@RestController
@RequestMapping("/system/configs")
@Tag(name = "系统配置接口")
@Validated
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigBiz configBiz;

    @SaCheckPermission("system:config:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询系统配置")
    public RestResult<PageResult<ConfigVo>> findConfigPage(
            @Valid @ModelAttribute ConfigQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ConfigVo> page = configBiz.findConfigPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("system:config:list")
    @GetMapping
    @Operation(summary = "查询系统配置列表")
    public RestResult<List<ConfigVo>> findConfigList(@Valid @ModelAttribute ConfigQueryVo queryVo) {
        List<ConfigVo> list = configBiz.findConfigList(queryVo);
        return ResultUtil.success(list);
    }

    @SaCheckPermission("system:config:detail")
    @GetMapping("/{key}")
    @Operation(summary = "获取系统配置详情")
    public RestResult<ConfigVo> getConfig(@Parameter(description = "配置键") @PathVariable String key) {
        return ResultUtil.success(configBiz.getConfig(key));
    }

    @SaCheckPermission("system:config:update")
    @PutMapping
    @Operation(summary = "更新系统配置")
    public RestResult<Void> updateConfig(@Valid @RequestBody ConfigUpdateVo updateVo) {
        configBiz.updateConfig(updateVo);
        return ResultUtil.success();
    }
}
