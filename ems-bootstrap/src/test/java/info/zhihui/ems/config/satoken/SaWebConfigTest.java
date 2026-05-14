package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SaWebConfigTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SaWebConfig saWebConfig;

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
                assertThat(requestContext.getOrganizationId()).isNull();
            } finally {
                RequestContextSetter.clear();
                StpUtil.logout();
            }
        });

        verify(userService, never()).getUserInfo(anyInt());
    }
}
