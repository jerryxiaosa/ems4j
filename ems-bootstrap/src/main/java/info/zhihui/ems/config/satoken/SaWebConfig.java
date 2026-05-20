package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 接口鉴权配置
 *
 * @author jerryxiaosa
 */
@Configuration
public class SaWebConfig implements WebMvcConfigurer {

    @Value("${permission.excludes:}")
    private String excludes;

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] excludePathPatterns = parseExcludePathPatterns(excludes);

        registry.addInterceptor(new SaInterceptor(handler -> {
                    // 设置用户上下文
                    // @SaIgnore不会进入
                    setUserContext();
                }) {
                    @Override
                    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
                        RequestContextSetter.clear();
                    }
                }).addPathPatterns("/**")
                .excludePathPatterns(excludePathPatterns);
    }

    private String[] parseExcludePathPatterns(String excludesConfig) {
        if (!StringUtils.hasText(excludesConfig)) {
            return new String[0];
        }
        return Arrays.stream(excludesConfig.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }

    private void setUserContext() {
        boolean mobileRequest = isMobileRequest();
        int userId = mobileRequest ? MobileStpUtil.getLoginIdAsInt() : StpUtil.getLoginIdAsInt();
        SaSession session = mobileRequest ? MobileStpUtil.getSession() : StpUtil.getSession();
        String userRealName = (String) session.get(LoginConstant.LOGIN_USER_REAL_NAME);
        String userPhone = (String) session.get(LoginConstant.LOGIN_USER_PHONE);

        if (!StringUtils.hasLength(userRealName) || !StringUtils.hasLength(userPhone)) {
            logoutCurrentRequest(mobileRequest);
            throw new BusinessRuntimeException("登录信息已失效，请重新登录");
        }

        UserRequestData userData = new UserRequestData(userRealName, userPhone);
        RequestContextSetter.doSet(userId, userData);
    }

    private boolean isMobileRequest() {
        String requestPath = SaHolder.getRequest().getRequestPath();
        if (!StringUtils.hasText(requestPath)) {
            return false;
        }
        return requestPath.equals("/v1/mini") || requestPath.startsWith("/v1/mini/");
    }

    private void logoutCurrentRequest(boolean mobileRequest) {
        if (mobileRequest) {
            MobileStpUtil.logout();
        } else {
            StpUtil.logout();
        }
    }
}
