package info.zhihui.ems.foundation.user.service;

import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.dto.CaptchaDto;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.LoginResponseDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 用户登录服务接口
 *
 * @author jerryxiaosa
 */
public interface LoginService {
    /**
     * 获取验证码
     *
     * @return 验证码
     */
    CaptchaDto getCaptcha();


    /**
     * 登录
     *
     * @param loginRequestDto 登录请求DTO
     * @return 登录响应DTO
     */
    LoginResponseDto login(@NotNull @Valid LoginRequestDto loginRequestDto);


    /**
     * 登出
     */
    void logout();


    /**
     * 获取登录用户菜单
     * @param menuSource 菜单来源
     *
     * @return 菜单列表
     */
    List<MenuBo> getLoginUserMenus(@NotNull MenuSourceEnum menuSource);
}
