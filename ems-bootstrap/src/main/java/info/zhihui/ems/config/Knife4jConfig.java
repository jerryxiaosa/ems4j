package info.zhihui.ems.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EMS Web API",
                version = "0.1.0",
                description = "EMS前端接口文档",
                contact = @Contact(name = "jerryxiaosa")
        )
)
public class Knife4jConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("EMS Web API")
                        .version("0.1.0")
                        .description("EMS前端接口文档")
                        .license(new License().name("Apache 2.0")));
    }
}

