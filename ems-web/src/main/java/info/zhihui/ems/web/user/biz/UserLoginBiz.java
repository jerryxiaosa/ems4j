package info.zhihui.ems.web.user.biz;

import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.CaptchaDto;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.LoginResponseDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.service.LoginService;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.web.user.mapstruct.UserLoginWebMapper;
import info.zhihui.ems.web.user.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录相关业务编排。
 */
@Service
@RequiredArgsConstructor
public class UserLoginBiz {

    private final LoginService loginService;
    private final UserService userService;
    private final UserLoginWebMapper userLoginWebMapper;

    /**
     * 获取登录验证码。
     *
     * @return 验证码信息
     */
    public CaptchaVo getCaptcha() {
        CaptchaDto captchaDto = loginService.getCaptcha();
        return userLoginWebMapper.toCaptchaVo(captchaDto);
    }

    /**
     * 执行登录。
     *
     * @param requestVo 登录请求参数
     * @return 登录响应信息
     */
    public LoginResponseVo login(LoginRequestVo requestVo) {
        LoginRequestDto requestDto = userLoginWebMapper.toLoginRequestDto(requestVo);
        LoginResponseDto responseDto = loginService.login(requestDto);
        return userLoginWebMapper.toLoginResponseVo(responseDto);
    }

    /**
     * 退出登录。
     */
    public void logout() {
        loginService.logout();
    }

    /**
     * 查询当前登录用户的菜单列表。
     *
     * @param source 菜单来源编码
     * @return 菜单 VO 列表
     */
    public List<UserMenuVo> findCurrentUserMenus(Integer source) {
        MenuSourceEnum menuSource = CodeEnum.fromCode(source, MenuSourceEnum.class);
        List<MenuBo> menus = loginService.getLoginUserMenus(menuSource);
        return userLoginWebMapper.toMenuVoList(menus);
    }

    /**
     * 查询当前登录用户信息。
     *
     * @return 用户信息
     */
    public UserVo findCurrentUser() {
        Integer loginId = StpUtil.getLoginIdAsInt();
        UserBo userInfo = userService.getUserInfo(loginId);
        return userLoginWebMapper.toUserVo(userInfo);
    }
}
