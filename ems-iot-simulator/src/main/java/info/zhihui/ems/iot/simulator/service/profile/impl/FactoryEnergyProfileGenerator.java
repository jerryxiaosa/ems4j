package info.zhihui.ems.iot.simulator.service.profile.impl;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.service.profile.EnergyProfileGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 工厂场景负载增量生成器。
 */
@Component
public class FactoryEnergyProfileGenerator implements EnergyProfileGenerator {

    private static final BigDecimal DAY_INCREMENT = new BigDecimal("5.60");
    private static final BigDecimal NIGHT_INCREMENT = new BigDecimal("4.90");

    @Override
    public ProfileTypeEnum getProfileType() {
        return ProfileTypeEnum.FACTORY;
    }

    @Override
    public BigDecimal generateTotalIncrement(LocalDateTime reportTime, SimulatorDeviceProperties deviceProperties) {
        BigDecimal baseIncrement = resolveBaseIncrement(reportTime);
        return applyRandomFactor(baseIncrement, reportTime, deviceProperties, 8);
    }

    private BigDecimal resolveBaseIncrement(LocalDateTime reportTime) {
        int hour = reportTime.getHour();
        if (hour >= 8 && hour < 20) {
            return DAY_INCREMENT;
        }
        return NIGHT_INCREMENT;
    }

    private BigDecimal applyRandomFactor(BigDecimal baseIncrement, LocalDateTime reportTime,
                                         SimulatorDeviceProperties deviceProperties, int variancePercent) {
        long deviceSeed = deviceProperties == null || deviceProperties.getRandomSeed() == null
                ? 0L : deviceProperties.getRandomSeed();
        long randomSeed = deviceSeed ^ reportTime.toLocalDate().toEpochDay() ^ ((long) reportTime.getHour() << 24);
        Random random = new Random(randomSeed);
        int factorValue = 100 - variancePercent + random.nextInt(variancePercent * 2 + 1);
        return baseIncrement.multiply(BigDecimal.valueOf(factorValue))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
