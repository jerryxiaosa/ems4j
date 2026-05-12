package info.zhihui.ems.business.mini.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.mini.auth.bo.MiniLoginBo;
import info.zhihui.ems.business.mini.auth.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mini.auth.service.MiniAuthService;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.service.LoginService;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
    private final LoginService loginService;

    @Override
    public MiniLoginResultBo login(@NotNull @Valid MiniLoginBo loginBo) {
        WechatMiniProgramLoginDto wechatLogin = wechatMiniProgramService.resolveLogin(
                loginBo.getLoginCode(),
                loginBo.getPhoneCode()
        );
        UserBo user = getUniqueUserByPhone(wechatLogin.getPurePhoneNumber());
        bindThirdPartyIdentity(user, wechatLogin);
        loginUser(user, wechatLogin);

        return new MiniLoginResultBo()
                .setAccessToken(StpUtil.getTokenValue())
                .setExpireIn(StpUtil.getTokenTimeout());
    }

    @Override
    public void logout() {
        loginService.logout();
    }

    private UserBo getUniqueUserByPhone(String phone) {
        List<UserBo> userList = userService.findUserList(new UserQueryDto().setUserPhone(phone));
        if (CollectionUtils.isEmpty(userList)) {
            throw new BusinessRuntimeException(ResultCode.MINI_PHONE_NOT_BOUND.getCode(), ResultCode.MINI_PHONE_NOT_BOUND.getMessage());
        }
        if (userList.size() > 1) {
            throw new BusinessRuntimeException(ResultCode.MINI_PHONE_BINDING_ABNORMAL.getCode(), ResultCode.MINI_PHONE_BINDING_ABNORMAL.getMessage());
        }
        UserBo user = userList.get(0);
        if (user.getId() == null) {
            throw new BusinessRuntimeException(ResultCode.MINI_USER_UNAVAILABLE.getCode(), ResultCode.MINI_USER_UNAVAILABLE.getMessage());
        }
        return user;
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

    private void loginUser(UserBo user, WechatMiniProgramLoginDto wechatLogin) {
        StpUtil.login(user.getId(), new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
        StpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, user.getRealName());
        StpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, user.getUserPhone());
        StpUtil.getSession().set(LoginConstant.LOGIN_MINI_OPEN_ID, wechatLogin.getOpenId());
        StpUtil.getSession().set(LoginConstant.LOGIN_MINI_UNION_ID, wechatLogin.getUnionId());
    }
}
