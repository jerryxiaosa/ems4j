package info.zhihui.ems.foundation.user;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.LoginException;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.LoginResponseDto;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import info.zhihui.ems.foundation.user.service.LoginService;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * LoginService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("登录服务集成测试")
class LoginServiceImplIntegrationTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("获取登录用户菜单 - 成功")
    void testGetLoginUserMenus_Success() {

        SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.login(1);
            try {
                List<MenuBo> menus = loginService.getLoginUserMenus(MenuSourceEnum.WEB);
                assertThat(menus).isNotEmpty();
                assertThat(menus).extracting(MenuBo::getMenuKey)
                        .contains("system", "user-manage", "user-add", "user-edit", "role-manage");
            } finally {
                try {
                    StpUtil.logout();
                } catch (NotLoginException ignore) {
                    // ignore for cleanup
                }
            }
        });

    }

    @Test
    @DisplayName("获取登录用户菜单 - 未登录成功")
    void testGetLoginUserMenus_NotLogin() {

        SaTokenContextMockUtil.setMockContext(() -> {
            assertThrows(BusinessRuntimeException.class, () -> loginService.getLoginUserMenus(MenuSourceEnum.WEB));
        });

    }

    @Test
    @DisplayName("登录成功-普通角色")
    void testLogin_Success() {
        String userName = "loginuser" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String phone = "139" + ThreadLocalRandom.current().nextInt(10000000, 99999999);
        UserCreateDto createDto = buildUserCreateDto(userName, phone);
        Integer userId = userService.add(createDto.setRoleIds(List.of(2)));

        LoginRequestDto request = new LoginRequestDto();
        request.setUserName(userName);
        request.setPassword(createDto.getPassword());
        request.setCaptchaKey("login-captcha");
        request.setCaptchaValue("1234");

        String captchaCacheKey = LoginConstant.CAPTCHA_CODE + request.getCaptchaKey();
        String pwdCacheKey = LoginConstant.PWD_ERR + userId;

        RedisUtil.setCacheObject(captchaCacheKey, "1234");
        RedisUtil.deleteObject(pwdCacheKey);

        SaTokenContextMockUtil.setMockContext(() -> {

            try {
                LoginResponseDto response = loginService.login(request);

                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isNotBlank();
                assertThat(StpUtil.isLogin()).isTrue();
                assertThat(StpUtil.getLoginIdAsInt()).isEqualTo(userId);
            } finally {
                try {
                    StpUtil.logout();
                } catch (NotLoginException ignore) {
                    // ignore for cleanup
                }
                RedisUtil.deleteObject(captchaCacheKey);
                RedisUtil.deleteObject(pwdCacheKey);
            }
        });
    }

    @Test
    @DisplayName("登录失败-验证码错误")
    void testLogin_CaptchaMismatch() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUserName("whoever");
        request.setPassword("ignored");
        request.setCaptchaKey("mismatch");
        request.setCaptchaValue("wrong");

        String captchaCacheKey = LoginConstant.CAPTCHA_CODE + request.getCaptchaKey();

        RedisUtil.setCacheObject(captchaCacheKey, "expected");
        try {
            assertThrows(LoginException.class, () -> loginService.login(request));
            assertThat(RedisUtil.hasKey(captchaCacheKey)).isFalse();
        } finally {
            RedisUtil.deleteObject(captchaCacheKey);
        }
    }

    private UserCreateDto buildUserCreateDto(String userName, String phone) {
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName(userName);
        dto.setPassword("LoginPass1!");
        dto.setRealName("登录测试用户");
        dto.setUserPhone(phone);
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setOrganizationId(1);
        dto.setCertificatesType(info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("33010119900101" + ThreadLocalRandom.current().nextInt(100, 999));
        dto.setRemark("login-test");
        return dto;
    }
}
