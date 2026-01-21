package info.zhihui.ems.web.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.system.biz.EnumBiz;
import info.zhihui.ems.web.system.vo.EnumItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统枚举查询接口：扫描所有实现 CodeEnum 的枚举并返回两级结构。
 */
@RestController
@RequestMapping("/system")
@Validated
@RequiredArgsConstructor
@Tag(name = "系统枚举接口")
public class EnumController {

    private final EnumBiz enumBiz;

    @SaCheckLogin
    @GetMapping("/enums")
    @Operation(summary = "扫描枚举列表", description = "扫描所有实现 CodeEnum 的枚举，返回枚举名及项列表（值、描述）")
    public RestResult<Map<String, List<EnumItemVo>>> findAllEnums() {
        Map<String, List<EnumItemVo>> map = enumBiz.getAll();
        return ResultUtil.success(map);
    }
}