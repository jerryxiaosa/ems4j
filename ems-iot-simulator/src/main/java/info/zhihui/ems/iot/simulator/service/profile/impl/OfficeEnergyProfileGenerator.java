package info.zhihui.ems.iot.simulator.service.profile.impl;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.service.profile.EnergyProfileGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 办公室场景负载增量生成器。
 */
@Component
public class OfficeEnergyProfileGenerator implements EnergyProfileGenerator {

    private static final BigDecimal WORKDAY_OFF_HOUR_INCREMENT = new BigDecimal("0.15");
    private static final BigDecimal WORKDAY_WORK_HOUR_INCREMENT = new BigDecimal("3.20");
    private static final BigDecimal WEEKEND_DAY_INCREMENT = new BigDecimal("0.40");

    @Override
    public ProfileTypeEnum getProfileType() {
        return ProfileTypeEnum.OFFICE;
    }

    @Override
    public BigDecimal generateTotalIncrement(LocalDateTime reportTime, SimulatorDeviceProperties deviceProperties) {
        BigDecimal baseIncrement = resolveBaseIncrement(reportTime);
        return applyRandomFactor(baseIncrement, reportTime, deviceProperties, 15);
    }

    private BigDecimal resolveBaseIncrement(LocalDateTime reportTime) {
        DayOfWeek dayOfWeek = reportTime.getDayOfWeek();
        int hour = reportTime.getHour();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return WEEKEND_DAY_INCREMENT;
        }
        if (hour >= 9 && hour < 18) {
            return WORKDAY_WORK_HOUR_INCREMENT;
        }
        return WORKDAY_OFF_HOUR_INCREMENT;
    }

    private BigDecimal applyRandomFactor(BigDecimal baseIncrement, LocalDateTime reportTime,
                                         SimulatorDeviceProperties deviceProperties, int variancePercent) {
        long randomSeed = buildRandomSeed(reportTime, deviceProperties);
        Random random = new Random(randomSeed);
        int factorValue = 100 - variancePercent + random.nextInt(variancePercent * 2 + 1);
        return baseIncrement.multiply(BigDecimal.valueOf(factorValue))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private long buildRandomSeed(LocalDateTime reportTime, SimulatorDeviceProperties deviceProperties) {
        long deviceSeed = deviceProperties == null || deviceProperties.getRandomSeed() == null
                ? 0L : deviceProperties.getRandomSeed();
        return deviceSeed ^ reportTime.toLocalDate().toEpochDay() ^ ((long) reportTime.getHour() << 32);
    }
}
