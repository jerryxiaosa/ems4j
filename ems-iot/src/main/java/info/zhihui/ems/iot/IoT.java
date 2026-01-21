package info.zhihui.ems.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 单体插件式 IoT 适配器示例入口。
 */
@SpringBootApplication
@EnableScheduling
public class IoT {

    public static void main(String[] args) {
        SpringApplication.run(IoT.class, args);
    }
}
