package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.model.EnergySnapshot;
import info.zhihui.ems.iot.simulator.service.profile.EnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.service.profile.impl.FactoryEnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.service.profile.impl.OfficeEnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergySimulationServiceTest {

    private final EnergySimulationService energySimulationService = new EnergySimulationService(
            List.of(new OfficeEnergyProfileGenerator(), new FactoryEnergyProfileGenerator()));

    @Test
    void testGenerateSnapshot_SwitchOff_ExpectedNoIncrementAndNoReport() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("OFF")
                .setLastTotalEnergy(new BigDecimal("100.00"))
                .setLastHigherEnergy(new BigDecimal("10.00"))
                .setLastHighEnergy(new BigDecimal("20.00"))
                .setLastLowEnergy(new BigDecimal("30.00"))
                .setLastLowerEnergy(new BigDecimal("25.00"))
                .setLastDeepLowEnergy(new BigDecimal("15.00"));

        EnergySnapshot snapshot = energySimulationService.generateSnapshot(
                runtimeState, device, LocalDateTime.of(2026, 3, 24, 10, 23, 15));

        assertFalse(snapshot.getShouldReport());
        assertEquals(new BigDecimal("100.00"), snapshot.getTotalEnergy());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getTotalEnergyIncrement());
    }

    @Test
    void testGenerateSnapshot_SwitchOn_ExpectedMonotonicEnergyAndConsistentRates() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setSwitchStatus("ON")
                .setLastTotalEnergy(new BigDecimal("100.00"))
                .setLastHigherEnergy(new BigDecimal("10.00"))
                .setLastHighEnergy(new BigDecimal("20.00"))
                .setLastLowEnergy(new BigDecimal("30.00"))
                .setLastLowerEnergy(new BigDecimal("25.00"))
                .setLastDeepLowEnergy(new BigDecimal("15.00"));

        EnergySnapshot snapshot = energySimulationService.generateSnapshot(
                runtimeState, device, LocalDateTime.of(2026, 3, 24, 10, 23, 15));

        assertTrue(snapshot.getShouldReport());
        assertTrue(snapshot.getTotalEnergy().compareTo(new BigDecimal("100.00")) > 0);
        BigDecimal sumRateIncrement = safe(snapshot.getIncrement().getHigherEnergyIncrement())
                .add(safe(snapshot.getIncrement().getHighEnergyIncrement()))
                .add(safe(snapshot.getIncrement().getLowEnergyIncrement()))
                .add(safe(snapshot.getIncrement().getLowerEnergyIncrement()))
                .add(safe(snapshot.getIncrement().getDeepLowEnergyIncrement()));
        assertEquals(0, sumRateIncrement.compareTo(snapshot.getIncrement().getTotalEnergyIncrement()));
    }

    @Test
    void testGenerateSnapshot_RuntimeStateNull_ExpectedSafeFirstSimulation() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);

        EnergySnapshot snapshot = energySimulationService.generateSnapshot(
                null, device, LocalDateTime.of(2026, 3, 24, 10, 23, 15));

        assertFalse(snapshot.getShouldReport());
        assertEquals(BigDecimal.ZERO, snapshot.getTotalEnergy());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getTotalEnergyIncrement());
    }

    @Test
    void testGenerateSnapshot_SameSeedAndReportTime_ExpectedDeterministicIncrement() {
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        device.setRandomSeed(101L);
        DeviceRuntimeState firstRuntimeState = new DeviceRuntimeState().setSwitchStatus("ON");
        DeviceRuntimeState secondRuntimeState = new DeviceRuntimeState().setSwitchStatus("ON");
        LocalDateTime reportTime = LocalDateTime.of(2026, 3, 24, 10, 23, 15);

        EnergySnapshot firstSnapshot = energySimulationService.generateSnapshot(firstRuntimeState, device, reportTime);
        EnergySnapshot secondSnapshot = energySimulationService.generateSnapshot(secondRuntimeState, device, reportTime);

        assertEquals(0, firstSnapshot.getIncrement().getTotalEnergyIncrement()
                .compareTo(secondSnapshot.getIncrement().getTotalEnergyIncrement()));
    }

    @Test
    void testGenerateSnapshot_HigherPeriod_ExpectedAssignOnlyHigherIncrement() {
        EnergySimulationService simulationService = new EnergySimulationService(List.of(new FixedIncrementGenerator(
                ProfileTypeEnum.OFFICE, new BigDecimal("3.25"))));
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        DeviceRuntimeState runtimeState = new DeviceRuntimeState().setSwitchStatus("ON");

        EnergySnapshot snapshot = simulationService.generateSnapshot(
                runtimeState, device, LocalDateTime.of(2026, 3, 24, 12, 23, 15));

        assertEquals(new BigDecimal("3.25"), snapshot.getIncrement().getTotalEnergyIncrement());
        assertEquals(new BigDecimal("3.25"), snapshot.getIncrement().getHigherEnergyIncrement());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getHighEnergyIncrement());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getLowEnergyIncrement());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getLowerEnergyIncrement());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getDeepLowEnergyIncrement());
    }

    @Test
    void testGenerateSnapshot_WhenGeneratorReturnsNullIncrement_ShouldUseZeroIncrement() {
        EnergySimulationService simulationService = new EnergySimulationService(List.of(new FixedIncrementGenerator(
                ProfileTypeEnum.OFFICE, null)));
        SimulatorDeviceProperties device = new SimulatorDeviceProperties();
        device.setProfileType(ProfileTypeEnum.OFFICE);
        DeviceRuntimeState runtimeState = new DeviceRuntimeState().setSwitchStatus("ON");

        EnergySnapshot snapshot = simulationService.generateSnapshot(
                runtimeState, device, LocalDateTime.of(2026, 3, 24, 19, 23, 15));

        assertTrue(snapshot.getShouldReport());
        assertEquals(BigDecimal.ZERO, snapshot.getIncrement().getTotalEnergyIncrement());
        assertEquals(BigDecimal.ZERO, snapshot.getTotalEnergy());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static final class FixedIncrementGenerator implements EnergyProfileGenerator {

        private final ProfileTypeEnum profileType;
        private final BigDecimal incrementAmount;

        private FixedIncrementGenerator(ProfileTypeEnum profileType, BigDecimal incrementAmount) {
            this.profileType = profileType;
            this.incrementAmount = incrementAmount;
        }

        @Override
        public ProfileTypeEnum getProfileType() {
            return profileType;
        }

        @Override
        public BigDecimal generateTotalIncrement(LocalDateTime reportTime, SimulatorDeviceProperties deviceProperties) {
            return incrementAmount;
        }
    }
}
