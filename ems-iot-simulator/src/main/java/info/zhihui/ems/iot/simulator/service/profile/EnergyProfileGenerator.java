package info.zhihui.ems.iot.simulator.service.profile;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 负载场景增量生成器。
 */
public interface EnergyProfileGenerator {

    ProfileTypeEnum getProfileType();

    BigDecimal generateTotalIncrement(LocalDateTime reportTime, SimulatorDeviceProperties deviceProperties);
}
