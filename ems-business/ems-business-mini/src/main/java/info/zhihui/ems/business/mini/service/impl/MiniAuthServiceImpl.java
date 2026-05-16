package info.zhihui.ems.business.mini.service.impl;

import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.mini.bo.MiniLoginBo;
import info.zhihui.ems.business.mini.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mini.service.MiniAuthService;
import info.zhihui.ems.business.mini.utils.MiniStpUtil;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 小程序认证服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class MiniAuthServiceImpl implements MiniAuthService {

    private final WechatMiniProgramService wechatMiniProgramService;
    private final UserService userService;
    private final UserThirdPartyBindService userThirdPartyBindService;

    @Override
    public MiniLoginResultBo login(@NotNull @Valid MiniLoginBo loginBo) {
        WechatMiniProgramLoginDto wechatLogin = wechatMiniProgramService.resolveLogin(
                loginBo.getLoginCode(),
                loginBo.getPhoneCode()
        );
        UserBo user = getMiniLoginUserByPhone(wechatLogin.getPurePhoneNumber());
        bindThirdPartyIdentity(user, wechatLogin);
        loginUser(user);

        return new MiniLoginResultBo()
                .setAccessToken(MiniStpUtil.getTokenValue())
                .setExpireIn(MiniStpUtil.getTokenTimeout());
    }

    @Override
    public void logout() {
        MiniStpUtil.logout();
    }

    private UserBo getMiniLoginUserByPhone(String phoneNumber) {
        try {
            return userService.getUserByPhone(phoneNumber);
        } catch (NotFoundException | BusinessRuntimeException e) {
            throw new BusinessRuntimeException(
                    ResultCode.MINI_PHONE_NOT_BOUND.getCode(),
                    ResultCode.MINI_PHONE_NOT_BOUND.getMessage()
            );
        }
    }

    private void bindThirdPartyIdentity(UserBo user, WechatMiniProgramLoginDto wechatLogin) {
        userThirdPartyBindService.bindOrUpdate(new UserThirdPartyBindDto()
                .setUserId(user.getId())
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId(wechatLogin.getAppId())
                .setThirdPartyUserId(wechatLogin.getOpenId())
                .setThirdPartyUnionId(wechatLogin.getUnionId())
                .setPhone(wechatLogin.getPurePhoneNumber()));
    }

    private void loginUser(UserBo user) {
        MiniStpUtil.login(user.getId(), new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
        MiniStpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, user.getRealName());
        MiniStpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, user.getUserPhone());
        if (user.getOrganizationId() != null) {
            MiniStpUtil.getSession().set(LoginConstant.LOGIN_USER_ORGANIZATION_ID, user.getOrganizationId());
        }
    }
}
