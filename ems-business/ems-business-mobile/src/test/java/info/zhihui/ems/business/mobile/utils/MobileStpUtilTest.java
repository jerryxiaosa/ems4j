package info.zhihui.ems.business.mobile.utils;

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

class MobileStpUtilTest {

    @BeforeEach
    void setUp() {
        SaManager.getConfig().setJwtSecretKey("mobile-stp-util-test-secret");
    }

    @Test
    void testGetStpLogic_ShouldUseMobileLoginType() {
        assertThat(MobileStpUtil.TYPE).isEqualTo("mobile");
        assertThat(MobileStpUtil.getStpLogic().getLoginType()).isEqualTo("mobile");
    }

    @Test
    void testLogin_ShouldExposeMobileTokenAndSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            try {
                MobileStpUtil.login(100, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));

                assertThat(MobileStpUtil.getLoginIdAsInt()).isEqualTo(100);
                assertThat(MobileStpUtil.getTokenValue()).isNotBlank();
                assertThat(MobileStpUtil.getTokenTimeout()).isNotZero();

                MobileStpUtil.getSession().set("mobile-session-key", "mobile-session-value");
                assertThat(MobileStpUtil.getSession().get("mobile-session-key")).isEqualTo("mobile-session-value");
            } finally {
                logoutMobileQuietly();
            }
        });
    }

    @Test
    void testMobileLogin_ShouldBeIndependentFromWebLogin() {
        SaTokenContextMockUtil.setMockContext(() -> {
            String webToken = null;
            String mobileToken = null;
            try {
                StpUtil.login(1);
                webToken = StpUtil.getTokenValue();
                MobileStpUtil.login(2, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
                mobileToken = MobileStpUtil.getTokenValue();

                assertThat(webToken).isNotBlank();
                assertThat(mobileToken).isNotBlank();
                assertThat(mobileToken).isNotEqualTo(webToken);
                assertThat(StpUtil.getStpLogic().getLoginIdByToken(webToken)).isEqualTo("1");
                assertThat(MobileStpUtil.getStpLogic().getLoginIdByToken(mobileToken)).isEqualTo("2");
                assertThat(StpUtil.getStpLogic().getLoginType()).isEqualTo("login");
                assertThat(MobileStpUtil.getStpLogic().getLoginType()).isEqualTo("mobile");
            } finally {
                logoutWebTokenQuietly(webToken);
                logoutMobileTokenQuietly(mobileToken);
            }
        });
    }

    @Test
    void testLogout_ShouldClearMobileLoginState() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MobileStpUtil.login(100, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));

            MobileStpUtil.logout();

            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt)
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

    private void logoutMobileQuietly() {
        try {
            MobileStpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }

    private void logoutMobileTokenQuietly(String tokenValue) {
        try {
            MobileStpUtil.getStpLogic().logoutByTokenValue(tokenValue);
        } catch (Exception ignore) {
            // ignore cleanup failure
        }
    }
}
