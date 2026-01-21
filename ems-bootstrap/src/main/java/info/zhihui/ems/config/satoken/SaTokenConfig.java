package info.zhihui.ems.config.satoken;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import info.zhihui.ems.foundation.user.satoken.RedisSaTokenDao;
import info.zhihui.ems.foundation.user.satoken.SaPermissionImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sa-token 配置
 *
 * @author Lion Li
 */
@AllArgsConstructor
@Configuration
public class SaTokenConfig {

    private final SaPermissionImpl saPermissionImpl;

    @Bean
    public StpLogic getStpLogicJwt() {
        // Sa-Token 整合 jwt (简单模式)
        return new StpLogicJwtForSimple();
    }

    /**
     * 权限接口实现(使用bean注入方便用户替换)
     */
    @Bean
    public StpInterface stpInterface() {
        return saPermissionImpl;
    }

    /**
     * 自定义dao层存储
     */
    @Bean
    public SaTokenDao saTokenDao() {
        return new RedisSaTokenDao();
    }

}
