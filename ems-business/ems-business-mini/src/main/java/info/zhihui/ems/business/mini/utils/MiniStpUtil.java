package info.zhihui.ems.business.mini.utils;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import lombok.Getter;

/**
 * 小程序端 Sa-Token 登录工具。
 */
public final class MiniStpUtil {

    public static final String TYPE = "mini";

    @Getter
    private static final StpLogic stpLogic = new StpLogicJwtForSimple(TYPE);

    private MiniStpUtil() {
    }

    public static void login(Object loginId, SaLoginParameter loginParameter) {
        stpLogic.login(loginId, loginParameter);
    }

    public static void logout() {
        stpLogic.logout();
    }

    public static int getLoginIdAsInt() {
        return stpLogic.getLoginIdAsInt();
    }

    public static String getTokenValue() {
        return stpLogic.getTokenValue();
    }

    public static long getTokenTimeout() {
        return stpLogic.getTokenTimeout();
    }

    public static SaSession getSession() {
        return stpLogic.getSession();
    }
}
