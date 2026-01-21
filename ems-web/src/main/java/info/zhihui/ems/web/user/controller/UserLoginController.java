package info.zhihui.ems.web.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.user.biz.UserLoginBiz;
import info.zhihui.ems.web.user.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
@Tag(name = "用户登录接口")
public class UserLoginController {

    private final UserLoginBiz userLoginBiz;

    @SaIgnore
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "生成登录验证码")
    public RestResult<CaptchaVo> getCaptcha() {
        CaptchaVo captcha = userLoginBiz.getCaptcha();
        return ResultUtil.success(captcha);
    }

    @SaIgnore
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名、密码及验证码执行登录操作")
    public RestResult<LoginResponseVo> login(@Valid @RequestBody LoginRequestVo loginRequestVo) {
        LoginResponseVo responseVo = userLoginBiz.login(loginRequestVo);
        return ResultUtil.success(responseVo);
    }

    @SaCheckLogin
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "注销当前会话用户")
    public RestResult<Void> logout() {
        userLoginBiz.logout();
        return ResultUtil.success();
    }

    @SaCheckLogin
    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息")
    public RestResult<UserVo> findCurrentUser() {
        UserVo userVo = userLoginBiz.findCurrentUser();
        return ResultUtil.success(userVo);
    }

    @SaCheckLogin
    @GetMapping("/current/menus")
    @Operation(summary = "查询当前登录用户菜单", description = "返回当前登录账户拥有的菜单列表")
    public RestResult<List<UserMenuVo>> findCurrentUserMenus(
            @Parameter(description = "菜单来源编码")
            @RequestParam Integer source) {
        List<UserMenuVo> menus = userLoginBiz.findCurrentUserMenus(source);
        return ResultUtil.success(menus);
    }
}
