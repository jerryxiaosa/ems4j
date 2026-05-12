package info.zhihui.ems.web.mini.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.mini.biz.MiniAuthBiz;
import info.zhihui.ems.web.mini.vo.MiniLoginRequestVo;
import info.zhihui.ems.web.mini.vo.MiniLoginResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序认证接口。
 */
@RestController
@RequestMapping(ApiPathConstant.V1 + "/mini/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "小程序认证接口")
public class MiniAuthController {

    private final MiniAuthBiz miniAuthBiz;

    @SaIgnore
    @PostMapping("/login")
    @Operation(summary = "微信手机号快捷登录")
    public RestResult<MiniLoginResponseVo> login(@Valid @RequestBody MiniLoginRequestVo requestVo) {
        return ResultUtil.success(miniAuthBiz.login(requestVo));
    }

    @SaCheckLogin
    @PostMapping("/logout")
    @Operation(summary = "小程序退出登录")
    public RestResult<Void> logout() {
        miniAuthBiz.logout();
        return ResultUtil.success();
    }
}
