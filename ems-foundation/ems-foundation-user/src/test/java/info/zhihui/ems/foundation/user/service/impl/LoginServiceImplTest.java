package info.zhihui.ems.foundation.user.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.extra.spring.SpringUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.LoginException;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.CaptchaDto;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.RoleEnum;
import info.zhihui.ems.foundation.user.qo.UserQueryQo;
import info.zhihui.ems.foundation.user.repository.UserRepository;
import info.zhihui.ems.foundation.user.service.MenuService;
import info.zhihui.ems.foundation.user.service.PasswordService;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServiceImpl 单元测试")
class LoginServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private MenuService menuService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private LoginServiceImpl loginService;

    private UserEntity userEntity;
    private UserBo userBo;
    private RoleSimpleBo normalRole;
    private RoleSimpleBo superRole;
    private RoleDetailBo roleDetailBo;
    private MenuBo menuBo;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setUserName("test");
        userEntity.setPassword("encoded");

        normalRole = new RoleSimpleBo().setId(2).setRoleKey("normal");
        superRole = new RoleSimpleBo().setId(3).setRoleKey(RoleEnum.SUPER_ADMIN.getCode());

        userBo = new UserBo().setId(1).setUserName("test");

        roleDetailBo = new RoleDetailBo().setId(2).setMenuIds(List.of(10, 11));

        menuBo = new MenuBo().setId(10).setMenuKey("menu");
    }

    @Test
    @DisplayName("getCaptcha 正常生成")
    void testGetCaptcha_Success() {
        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            when(codeGenerator.generate()).thenReturn("7-3=");
            CaptchaDto dto = loginService.getCaptcha();

            assertThat(dto.getCaptchaKey()).isNotBlank();
            assertThat(dto.getImg()).isNotBlank();

            redisMock.verify(() -> RedisUtil.setCacheObject(startsWith("user::captcha::"), anyString(), eq(Duration.ofMinutes(1))));
        }
    }

    @Test
    @DisplayName("login 成功 - 普通角色")
    void testLogin_SuccessNormal() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("test");
        request.setPassword("123456");
        request.setCaptchaKey("key");
        request.setCaptchaValue("3");

        userBo.setRoles(List.of(normalRole));

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class);
             MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {

            redisMock.when(() -> RedisUtil.getCacheObject("user::captcha::" + request.getCaptchaKey())).thenReturn("5-2=");
            redisMock.when(() -> RedisUtil.deleteObject("user::captcha::" + request.getCaptchaKey())).thenReturn(true);

            when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(List.of(userEntity));
            when(passwordService.matchesPassword("123456", "encoded")).thenReturn(true);
            when(userService.getUserInfo(1)).thenReturn(userBo);
            when(codeGenerator.verify("5-2=", "3")).thenReturn(true);

            loginService.login(request);

            stpMock.verify(() -> StpUtil.login(eq(1), any(SaLoginParameter.class)));
        }
    }

    @Test
    @DisplayName("login 成功 - 超级管理员角色")
    void testLogin_SuccessSuperAdmin() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("admin");
        request.setPassword("123456");
        request.setCaptchaKey("key");
        request.setCaptchaValue("18");

        userBo.setRoles(List.of(superRole));

        try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
            RedissonClient redisson = mock(RedissonClient.class);
            springMock.when(() -> SpringUtil.getBean(eq(RedissonClient.class)))
                    .thenReturn(redisson);

            try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class);
                 MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {

                redisMock.when(() -> RedisUtil.getCacheObject("user::captcha::" + request.getCaptchaKey())).thenReturn("3*6=");
                redisMock.when(() -> RedisUtil.deleteObject("user::captcha::" + request.getCaptchaKey())).thenReturn(true);

                when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(List.of(userEntity));
                when(passwordService.matchesPassword("123456", "encoded")).thenReturn(true);
                when(userService.getUserInfo(1)).thenReturn(userBo);
                when(codeGenerator.verify("3*6=", "18")).thenReturn(true);

                loginService.login(request);

                stpMock.verify(() -> StpUtil.login(eq(1), any(SaLoginParameter.class)));
            }
        }
    }

    @Test
    @DisplayName("login 失败 - 验证码为空")
    void testLogin_CaptchaNull() {
        LoginRequestDto request = new LoginRequestDto();
        request.setCaptchaValue(null);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(LoginException.class);
    }

    @Test
    @DisplayName("login 失败 - 验证码过期")
    void testLogin_CaptchaExpired() {
        LoginRequestDto request = new LoginRequestDto();
        request.setCaptchaKey("key");
        request.setCaptchaValue("value");

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject("user::captcha::" + request.getCaptchaKey())).thenReturn(null);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(BusinessRuntimeException.class)
                    .hasMessageContaining("验证码已过期");
        }
    }

    @Test
    @DisplayName("login 失败 - 验证码错误")
    void testLogin_CaptchaError() {
        LoginRequestDto request = new LoginRequestDto();
        request.setCaptchaKey("key");
        request.setCaptchaValue("wrong");

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject("user::captcha::" + request.getCaptchaKey())).thenReturn("value");
            redisMock.when(() -> RedisUtil.deleteObject("user::captcha::" + request.getCaptchaKey())).thenReturn(true);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(LoginException.class);
        }
    }

    @Test
    @DisplayName("login 失败 - 用户不存在")
    void testLogin_UserNotFound() {
        LoginRequestDto request = baseLoginRequest();

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(any())).thenReturn("value");
            redisMock.when(() -> RedisUtil.deleteObject(any(String.class))).thenReturn(true);

            when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(Collections.emptyList());
            when(codeGenerator.verify(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(LoginException.class)
                    .hasMessageContaining("账号或密码错误");
        }
    }

    @Test
    @DisplayName("login 失败 - 账号重复")
    void testLogin_AccountRepeat() {
        LoginRequestDto request = baseLoginRequest();

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(any())).thenReturn("value");
            redisMock.when(() -> RedisUtil.deleteObject(any(String.class))).thenReturn(true);

            when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(List.of(userEntity, new UserEntity()));
            when(codeGenerator.verify(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(LoginException.class)
                    .hasMessageContaining("账号重复");
        }
    }

    @Test
    @DisplayName("login 失败 - 密码错误")
    void testLogin_PasswordError() {
        LoginRequestDto request = baseLoginRequest();

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(any())).thenReturn("value");
            redisMock.when(() -> RedisUtil.deleteObject(any(String.class))).thenReturn(true);

            when(codeGenerator.verify(any(), any())).thenReturn(true);
            when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(List.of(userEntity));
            when(passwordService.matchesPassword(any(), any())).thenReturn(false);
            redisMock.when(() -> RedisUtil.getCacheObject("user::pwd_err::1")).thenReturn(0);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(LoginException.class);
        }
    }

    @Test
    @DisplayName("login 失败 - 无角色")
    void testLogin_NoRole() {
        LoginRequestDto request = baseLoginRequest();

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            commonSuccessMocks(redisMock);
            userBo.setRoles(Collections.emptyList());
            when(userService.getUserInfo(1)).thenReturn(userBo);
            when(codeGenerator.verify(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(LoginException.class)
                    .hasMessageContaining("未分配功能权限");
        }
    }

    @Test
    @DisplayName("logout 正常退出")
    void testLogout_Success() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            loginService.logout();
            stpMock.verify(StpUtil::logout);
        }
    }

    @Test
    @DisplayName("logout 未登录")
    void testLogout_NotLogin() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::logout).thenThrow(new NotLoginException("no login", "web", "web"));

            loginService.logout();
        }
    }

    @Test
    @DisplayName("logout 其它异常")
    void testLogout_Exception() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::logout).thenThrow(new RuntimeException("something else"));

            assertThatThrownBy(() -> loginService.logout())
                    .isInstanceOf(BusinessRuntimeException.class)
                    .hasMessageContaining("退出登录异常");
        }
    }

    @Test
    @DisplayName("getLoginUserMenus 成功")
    void testGetLoginUserMenus_Success() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);
            userBo.setRoles(List.of(normalRole));
            when(userService.getUserInfo(1)).thenReturn(userBo);
            when(roleService.getDetail(2)).thenReturn(roleDetailBo);
            when(menuService.findList(any(MenuQueryDto.class))).thenReturn(List.of(menuBo));

            List<MenuBo> menus = loginService.getLoginUserMenus(MenuSourceEnum.WEB);
            assertThat(menus).hasSize(1);
        }
    }

    @Test
    @DisplayName("getLoginUserMenus 未登录")
    void testGetLoginUserMenus_NotLogin() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsInt).thenThrow(new NotLoginException("not login", "web", "web"));

            assertThatThrownBy(() -> loginService.getLoginUserMenus(MenuSourceEnum.WEB))
                    .isInstanceOf(BusinessRuntimeException.class)
                    .hasMessageContaining("获取登录用户信息异常");
        }
    }

    @Test
    @DisplayName("getLoginUserMenus 无菜单")
    void testGetLoginUserMenus_NoMenu() {
        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            stpMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);
            userBo.setRoles(List.of(normalRole));
            when(userService.getUserInfo(1)).thenReturn(userBo);
            when(roleService.getDetail(2)).thenReturn(roleDetailBo);
            when(menuService.findList(any(MenuQueryDto.class))).thenReturn(Collections.emptyList());

            List<MenuBo> menus = loginService.getLoginUserMenus(MenuSourceEnum.WEB);
            assertThat(menus).isEmpty();
        }
    }

    private LoginRequestDto baseLoginRequest() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("test");
        request.setPassword("123456");
        request.setCaptchaKey("key");
        request.setCaptchaValue("value");
        return request;
    }

    private void commonSuccessMocks(MockedStatic<RedisUtil> redisMock) {
        redisMock.when(() -> RedisUtil.getCacheObject("user::captcha::key")).thenReturn("value");
        redisMock.when(() -> RedisUtil.deleteObject("user::captcha::key")).thenReturn(true);
        when(userRepository.selectByQo(any(UserQueryQo.class))).thenReturn(List.of(userEntity));
        when(passwordService.matchesPassword("123456", "encoded")).thenReturn(true);
    }
}
