package info.zhihui.ems.iot.simulator.service.profile;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.service.profile.impl.OfficeEnergyProfileGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OfficeEnergyProfileGeneratorTest {

    private final OfficeEnergyProfileGenerator generator = new OfficeEnergyProfileGenerator();

    @Test
    void testGenerateTotalIncrement_WorkdayWorkhour_ExpectedGreaterThanOffhour() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);

        BigDecimal workhourIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 24, 10, 23, 15), device);
        BigDecimal offhourIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 24, 22, 23, 15), device);

        assertTrue(workhourIncrement.compareTo(offhourIncrement) > 0);
    }

    @Test
    void testGenerateTotalIncrement_WeekendDaytime_ExpectedLowerThanWorkday() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);

        BigDecimal workdayIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 24, 10, 23, 15), device);
        BigDecimal weekendIncrement = generator.generateTotalIncrement(
                LocalDateTime.of(2026, 3, 28, 10, 23, 15), device);

        assertTrue(workdayIncrement.compareTo(weekendIncrement) > 0);
    }
}
