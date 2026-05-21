package info.zhihui.ems.business.mobile.satoken.listener;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.event.UserDeletedEvent;
import info.zhihui.ems.foundation.user.event.UserPasswordResetEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MobileUserLoginStateCleanupListenerTest {

    @Test
    void onUserDeleted_WhenMobileSessionExists_ShouldLogoutMobileSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobile(21);
            MobileUserLoginStateCleanupListener listener = new MobileUserLoginStateCleanupListener();

            listener.onUserDeleted(new UserDeletedEvent(21));

            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    @Test
    void onUserPasswordReset_WhenMobileSessionExists_ShouldLogoutMobileSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobile(22);
            MobileUserLoginStateCleanupListener listener = new MobileUserLoginStateCleanupListener();

            listener.onUserPasswordReset(new UserPasswordResetEvent(22));

            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    @Test
    void onUserDeleted_WhenWebAndMobileSessionsExist_ShouldOnlyLogoutMobileSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            SaManager.getConfig().setJwtSecretKey("mobile-user-cleanup-listener-test-secret");
            String webToken = null;
            try {
                StpUtil.login(24);
                webToken = StpUtil.getTokenValue();
                loginMobile(24);
                MobileUserLoginStateCleanupListener listener = new MobileUserLoginStateCleanupListener();

                listener.onUserDeleted(new UserDeletedEvent(24));

                assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
                assertThat(StpUtil.getStpLogic().getLoginIdByToken(webToken)).isEqualTo("24");
            } finally {
                logoutWebTokenQuietly(webToken);
            }
        });
    }

    @Test
    void onUserDeleted_WhenMobileSessionMissing_ShouldSkipSilently() {
        MobileUserLoginStateCleanupListener listener = new MobileUserLoginStateCleanupListener();

        assertThatCode(() -> listener.onUserDeleted(new UserDeletedEvent(23))).doesNotThrowAnyException();
    }

    private void loginMobile(Integer userId) {
        SaManager.getConfig().setJwtSecretKey("mobile-user-cleanup-listener-test-secret");
        MobileStpUtil.login(userId, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
    }

    private void logoutWebTokenQuietly(String tokenValue) {
        try {
            StpUtil.getStpLogic().logoutByTokenValue(tokenValue);
        } catch (Exception ignore) {
            // ignore cleanup failure
        }
    }
}
