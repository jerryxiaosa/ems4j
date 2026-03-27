package info.zhihui.ems.iot.simulator;

import info.zhihui.ems.components.datasource.config.MybatisPlusConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Map;

/**
 * IoT 模拟器命令行入口。
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusConfig.class
})
public class IotSimulator {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(IotSimulator.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        application.run(args);
    }

}
