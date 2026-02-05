package info.zhihui.ems.components.datasource.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import info.zhihui.ems.common.factory.YmlPropertySourceFactory;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.datasource.handler.AutoFillHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;

@MapperScan(basePackages = "${mybatis-plus.repositoryPackage:info.zhihui.ems.**.repository}")
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@PropertySource(value = "classpath:common-mybatis.yml", factory = YmlPropertySourceFactory.class)
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }

    @Bean
    public MetaObjectHandler getAutoFillHandler(RequestContext requestContext) {
        return new AutoFillHandler(requestContext);
    }

}
