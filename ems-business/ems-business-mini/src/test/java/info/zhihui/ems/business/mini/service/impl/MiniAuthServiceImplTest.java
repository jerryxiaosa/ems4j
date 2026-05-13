package info.zhihui.ems.business.mini.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.mini.bo.MiniLoginBo;
import info.zhihui.ems.business.mini.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mini.utils.MiniStpUtil;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniAuthServiceImplTest {

    @Mock
    private WechatMiniProgramService wechatMiniProgramService;
    @Mock
    private UserService userService;
    @Mock
    private UserThirdPartyBindService userThirdPartyBindService;

    @InjectMocks
    private MiniAuthServiceImpl miniAuthService;

    @Test
    void login_WhenUnionIdMissing_ShouldSkipUnionIdSessionValue() {
        MiniLoginBo loginBo = new MiniLoginBo()
                .setLoginCode("login-code")
                .setPhoneCode("phone-code");
        WechatMiniProgramLoginDto wechatLogin = new WechatMiniProgramLoginDto()
                .setAppId("app-id")
                .setOpenId("open-id")
                .setUnionId(null)
                .setPurePhoneNumber("13800138000");
        UserBo user = new UserBo()
                .setId(1)
                .setRealName("测试用户")
                .setUserPhone("13800138000")
                .setOrganizationId(10);
        SaSession session = mock(SaSession.class);

        when(wechatMiniProgramService.resolveLogin("login-code", "phone-code")).thenReturn(wechatLogin);
        when(userService.getUserByPhone("13800138000")).thenReturn(user);

        try (MockedStatic<MiniStpUtil> miniStpMock = mockStatic(MiniStpUtil.class)) {
            miniStpMock.when(MiniStpUtil::getSession).thenReturn(session);
            miniStpMock.when(MiniStpUtil::getTokenValue).thenReturn("mini-token");
            miniStpMock.when(MiniStpUtil::getTokenTimeout).thenReturn(7200L);

            miniAuthService.login(loginBo);

            miniStpMock.verify(() -> MiniStpUtil.login(eq(1), any(SaLoginParameter.class)));
            verify(userService).getUserByPhone("13800138000");
            verify(session).set(LoginConstant.LOGIN_USER_REAL_NAME, "测试用户");
            verify(session).set(LoginConstant.LOGIN_USER_PHONE, "13800138000");
            verify(session).set(LoginConstant.LOGIN_USER_ORGANIZATION_ID, 10);
            verify(session).set(LoginConstant.LOGIN_MINI_OPEN_ID, "open-id");
            verify(session, never()).set(eq(LoginConstant.LOGIN_MINI_UNION_ID), any());
        }
    }

    @Test
    void login_WhenUnionIdExists_ShouldBindIdentityAndReturnToken() {
        MiniLoginBo loginBo = new MiniLoginBo()
                .setLoginCode("login-code")
                .setPhoneCode("phone-code");
        WechatMiniProgramLoginDto wechatLogin = new WechatMiniProgramLoginDto()
                .setAppId("wx-app")
                .setOpenId("open-id")
                .setUnionId("union-id")
                .setPurePhoneNumber("13800138000");
        UserBo user = new UserBo()
                .setId(2)
                .setRealName("张三")
                .setUserPhone("13800138000");
        SaSession session = mock(SaSession.class);

        when(wechatMiniProgramService.resolveLogin("login-code", "phone-code")).thenReturn(wechatLogin);
        when(userService.getUserByPhone("13800138000")).thenReturn(user);

        try (MockedStatic<MiniStpUtil> miniStpMock = mockStatic(MiniStpUtil.class)) {
            miniStpMock.when(MiniStpUtil::getSession).thenReturn(session);
            miniStpMock.when(MiniStpUtil::getTokenValue).thenReturn("mini-token");
            miniStpMock.when(MiniStpUtil::getTokenTimeout).thenReturn(7200L);

            MiniLoginResultBo result = miniAuthService.login(loginBo);

            assertThat(result.getAccessToken()).isEqualTo("mini-token");
            assertThat(result.getExpireIn()).isEqualTo(7200L);
            miniStpMock.verify(() -> MiniStpUtil.login(eq(2), any(SaLoginParameter.class)));
            verify(session).set(LoginConstant.LOGIN_USER_REAL_NAME, "张三");
            verify(session).set(LoginConstant.LOGIN_USER_PHONE, "13800138000");
            verify(session, never()).set(eq(LoginConstant.LOGIN_USER_ORGANIZATION_ID), any());
            verify(session).set(LoginConstant.LOGIN_MINI_OPEN_ID, "open-id");
            verify(session).set(LoginConstant.LOGIN_MINI_UNION_ID, "union-id");
        }

        ArgumentCaptor<UserThirdPartyBindDto> bindCaptor = ArgumentCaptor.forClass(UserThirdPartyBindDto.class);
        verify(userThirdPartyBindService).bindOrUpdate(bindCaptor.capture());
        UserThirdPartyBindDto bindDto = bindCaptor.getValue();
        assertThat(bindDto.getUserId()).isEqualTo(2);
        assertThat(bindDto.getPlatform()).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI);
        assertThat(bindDto.getAppId()).isEqualTo("wx-app");
        assertThat(bindDto.getThirdPartyUserId()).isEqualTo("open-id");
        assertThat(bindDto.getThirdPartyUnionId()).isEqualTo("union-id");
        assertThat(bindDto.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void logout_ShouldDelegateToMiniStpUtil() {
        try (MockedStatic<MiniStpUtil> miniStpMock = mockStatic(MiniStpUtil.class)) {
            miniAuthService.logout();

            miniStpMock.verify(MiniStpUtil::logout);
        }
    }
}
