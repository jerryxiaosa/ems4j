package info.zhihui.ems.business.mobile.satoken.listener;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.event.UserProfileUpdatedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MobileUserProfileUpdatedListenerTest {

    @Test
    void onUserProfileUpdated_WhenMobileSessionExists_ShouldRefreshBaseUserInfo() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobile(11);
            MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, "旧姓名");
            MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, "13800138000");
            MobileUserProfileUpdatedListener listener = new MobileUserProfileUpdatedListener();

            listener.onUserProfileUpdated(new UserProfileUpdatedEvent(11, "新姓名", "13900139000"));

            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_REAL_NAME)).isEqualTo("新姓名");
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_PHONE)).isEqualTo("13900139000");
            logoutMobileQuietly();
        });
    }

    @Test
    void onUserProfileUpdated_WhenMobileSessionMissing_ShouldSkipSilently() {
        MobileUserProfileUpdatedListener listener = new MobileUserProfileUpdatedListener();

        assertThatCode(() -> listener.onUserProfileUpdated(new UserProfileUpdatedEvent(12, "新姓名", "13900139000")))
                .doesNotThrowAnyException();
    }

    private void loginMobile(Integer userId) {
        SaManager.getConfig().setJwtSecretKey("mobile-user-profile-listener-test-secret");
        MobileStpUtil.login(userId, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
    }

    private void logoutMobileQuietly() {
        try {
            MobileStpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }
}
