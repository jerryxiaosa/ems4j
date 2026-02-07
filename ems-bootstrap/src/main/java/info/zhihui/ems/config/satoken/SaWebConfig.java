package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.service.UserService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Configuration
public class SaWebConfig implements WebMvcConfigurer {

    private final UserService userService;

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
        int userId = StpUtil.getLoginIdAsInt();
        String userRealName = (String) StpUtil.getSession().get(LoginConstant.LOGIN_USER_REAL_NAME);
        String userPhone = (String) StpUtil.getSession().get(LoginConstant.LOGIN_USER_PHONE);

        if (!StringUtils.hasLength(userRealName) || !StringUtils.hasLength(userPhone)) {
            UserBo user;
            try {
                // 登录会话没有用户基础信息时回源，并写回会话
                user = userService.getUserInfo(userId);
            } catch (NotFoundException e) {
                StpUtil.logout();
                throw new BusinessRuntimeException("无法获取到用户信息");
            }
            userRealName = user.getRealName();
            userPhone = user.getUserPhone();
            StpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, userRealName);
            StpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, userPhone);
        }

        UserRequestData userData = new UserRequestData(userRealName, userPhone);
        RequestContextSetter.doSet(userId, userData);
    }
}
