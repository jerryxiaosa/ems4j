package info.zhihui.ems.iot.simulator.service.profile;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.service.profile.impl.FactoryEnergyProfileGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FactoryEnergyProfileGeneratorTest {

    private final FactoryEnergyProfileGenerator generator = new FactoryEnergyProfileGenerator();

    @Test
    void testGenerateTotalIncrement_DayAndNight_ExpectedBothPositiveAndDaySlightlyHigher() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.FACTORY);
        device.setRandomSeed(202L);

        BigDecimal dayIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 24, 10, 23, 15), device);
        BigDecimal nightIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 24, 2, 23, 15), device);

        assertTrue(dayIncrement.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(nightIncrement.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(dayIncrement.compareTo(nightIncrement) > 0);
    }
}
