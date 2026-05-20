package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.context.mock.SaRequestForMock;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SaWebConfigTest {

    private final SaWebConfig saWebConfig = new SaWebConfig();

    @Test
    void setUserContext_WhenUserHasNoOrganizationButSessionHasBaseInfo_ShouldNotBackfillUserInfo() {
        SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.login(100);
            try {
                StpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, "管理员");
                StpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, "13800138000");

                ReflectionTestUtils.invokeMethod(saWebConfig, "setUserContext");

                RequestContext requestContext = new RequestContext();
                assertThat(requestContext.getUserId()).isEqualTo(100);
                assertThat(requestContext.getUserRealName()).isEqualTo("管理员");
                assertThat(requestContext.getUserPhone()).isEqualTo("13800138000");
                assertThat(requestContext.getAccountId()).isNull();
            } finally {
                RequestContextSetter.clear();
                StpUtil.logout();
            }
        });
    }

    @Test
    void setUserContext_WhenMobileSessionHasNoAccount_ShouldOnlySetBaseUserInfo() {
        SaTokenContextMockUtil.setMockContext(() -> {
            SaManager.getConfig().setJwtSecretKey("sa-web-config-mobile-test-secret");
            ((SaRequestForMock) SaHolder.getRequest()).requestPath = "/v1/mini/home/trend";
            MobileStpUtil.login(200, new cn.dev33.satoken.stp.parameter.SaLoginParameter()
                    .setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
            try {
                MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, "移动用户");
                MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, "13900139000");

                ReflectionTestUtils.invokeMethod(saWebConfig, "setUserContext");

                RequestContext requestContext = new RequestContext();
                assertThat(requestContext.getUserId()).isEqualTo(200);
                assertThat(requestContext.getUserRealName()).isEqualTo("移动用户");
                assertThat(requestContext.getUserPhone()).isEqualTo("13900139000");
                assertThat(requestContext.getAccountId()).isNull();
            } finally {
                RequestContextSetter.clear();
                MobileStpUtil.logout();
            }
        });
    }
}
