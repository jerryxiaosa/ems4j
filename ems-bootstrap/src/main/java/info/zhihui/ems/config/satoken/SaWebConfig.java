package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 接口鉴权配置
 *
 * @author jerryxiaosa
 */
@RequiredArgsConstructor
@Configuration
public class SaWebConfig implements WebMvcConfigurer {

    private final UserService userService;

    @Value("${permission.excludes}")
    private String excludes;

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

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
                .excludePathPatterns(excludes.split(","));
    }

    private void setUserContext() {
        int userId = StpUtil.getLoginIdAsInt();
        UserBo user;
        try {
            // 获取最新的用户信息
            user = userService.getUserInfo(userId);
        } catch (NotFoundException e) {
            StpUtil.logout();
            throw new BusinessRuntimeException("无法获取到用户信息");
        }

        UserRequestData userData = new UserRequestData(user.getRealName(), user.getUserPhone());
        RequestContextSetter.doSet(userId, userData);
    }
}
