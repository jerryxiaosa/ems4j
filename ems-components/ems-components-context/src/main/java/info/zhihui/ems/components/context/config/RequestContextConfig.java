package info.zhihui.ems.components.context.config;

import info.zhihui.ems.components.context.RequestContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RequestContextConfig {
    @Bean
    public RequestContext getRequestContext() {
        return new RequestContext();
    }
}
