package info.zhihui.ems.business.mini.utils;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MiniStpUtilTest {

    @BeforeEach
    void setUp() {
        SaManager.getConfig().setJwtSecretKey("mini-stp-util-test-secret");
    }

    @Test
    void testGetStpLogic_ShouldUseMiniLoginType() {
        assertThat(MiniStpUtil.TYPE).isEqualTo("mini");
        assertThat(MiniStpUtil.getStpLogic().getLoginType()).isEqualTo("mini");
    }

    @Test
    void testLogin_ShouldExposeMiniTokenAndSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            try {
                MiniStpUtil.login(100, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));

                assertThat(MiniStpUtil.getLoginIdAsInt()).isEqualTo(100);
                assertThat(MiniStpUtil.getTokenValue()).isNotBlank();
                assertThat(MiniStpUtil.getTokenTimeout()).isNotZero();

                MiniStpUtil.getSession().set("mini-session-key", "mini-session-value");
                assertThat(MiniStpUtil.getSession().get("mini-session-key")).isEqualTo("mini-session-value");
            } finally {
                logoutMiniQuietly();
            }
        });
    }

    @Test
    void testMiniLogin_ShouldBeIndependentFromWebLogin() {
        SaTokenContextMockUtil.setMockContext(() -> {
            String webToken = null;
            String miniToken = null;
            try {
                StpUtil.login(1);
                webToken = StpUtil.getTokenValue();
                MiniStpUtil.login(2, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
                miniToken = MiniStpUtil.getTokenValue();

                assertThat(webToken).isNotBlank();
                assertThat(miniToken).isNotBlank();
                assertThat(miniToken).isNotEqualTo(webToken);
                assertThat(StpUtil.getStpLogic().getLoginIdByToken(webToken)).isEqualTo("1");
                assertThat(MiniStpUtil.getStpLogic().getLoginIdByToken(miniToken)).isEqualTo("2");
                assertThat(StpUtil.getStpLogic().getLoginType()).isEqualTo("login");
                assertThat(MiniStpUtil.getStpLogic().getLoginType()).isEqualTo("mini");
            } finally {
                logoutWebTokenQuietly(webToken);
                logoutMiniTokenQuietly(miniToken);
            }
        });
    }

    @Test
    void testLogout_ShouldClearMiniLoginState() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniStpUtil.login(100, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));

            MiniStpUtil.logout();

            assertThatThrownBy(MiniStpUtil::getLoginIdAsInt)
                    .isInstanceOf(NotLoginException.class);
        });
    }

    private void logoutWebQuietly() {
        try {
            StpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }

    private void logoutWebTokenQuietly(String tokenValue) {
        try {
            StpUtil.getStpLogic().logoutByTokenValue(tokenValue);
        } catch (Exception ignore) {
            // ignore cleanup failure
        }
    }

    private void logoutMiniQuietly() {
        try {
            MiniStpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }

    private void logoutMiniTokenQuietly(String tokenValue) {
        try {
            MiniStpUtil.getStpLogic().logoutByTokenValue(tokenValue);
        } catch (Exception ignore) {
            // ignore cleanup failure
        }
    }
}
