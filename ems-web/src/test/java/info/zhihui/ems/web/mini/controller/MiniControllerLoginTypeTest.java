package info.zhihui.ems.web.mini.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiniControllerLoginTypeTest {

    @Test
    void miniProtectedEndpoints_ShouldUseMiniLoginType() throws NoSuchMethodException {
        assertMiniLoginType(MiniMeController.class.getMethod("getCurrentUser"));
        assertMiniLoginType(MiniAuthController.class.getMethod("logout"));
        assertMiniLoginType(MiniHomeController.class.getMethod("getSummary"));
        assertMiniLoginType(MiniHomeController.class.getMethod("getTrend", String.class));
    }

    private void assertMiniLoginType(Method method) {
        SaCheckLogin annotation = method.getAnnotation(SaCheckLogin.class);
        assertEquals("mobile", annotation.type());
    }
}
