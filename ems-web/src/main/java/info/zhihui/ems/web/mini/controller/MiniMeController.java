package info.zhihui.ems.web.mini.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import info.zhihui.ems.business.mini.utils.MiniStpUtil;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.mini.biz.MiniMeBiz;
import info.zhihui.ems.web.mini.vo.MiniCurrentUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序当前用户接口。
 */
@RestController
@RequestMapping(ApiPathConstant.V1 + "/mini")
@RequiredArgsConstructor
@Tag(name = "小程序当前用户接口")
public class MiniMeController {

    private final MiniMeBiz miniMeBiz;

    @SaCheckLogin(type = MiniStpUtil.TYPE)
    @GetMapping("/me")
    @Operation(summary = "查询当前用户与开户账户信息")
    public RestResult<MiniCurrentUserVo> getCurrentUser() {
        return ResultUtil.success(miniMeBiz.getCurrentUser());
    }
}
