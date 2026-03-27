package info.zhihui.ems.iot.simulator.config;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模拟器运行时装配。
 */
@Configuration
public class SimulatorConfiguration {

    @Bean
    Acrel4gFrameCodec acrel4gFrameCodec() {
        return new Acrel4gFrameCodec();
    }
}
