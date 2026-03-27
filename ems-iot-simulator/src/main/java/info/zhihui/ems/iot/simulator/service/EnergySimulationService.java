package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.model.EnergyIncrement;
import info.zhihui.ems.iot.simulator.model.EnergySnapshot;
import info.zhihui.ems.iot.simulator.service.profile.EnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电量模拟服务。
 */
@Service
public class EnergySimulationService {

    private final Map<ProfileTypeEnum, EnergyProfileGenerator> profileGeneratorMap;

    public EnergySimulationService(List<EnergyProfileGenerator> generators) {
        this.profileGeneratorMap = new HashMap<>();
        if (generators == null) {
            return;
        }
        for (EnergyProfileGenerator generator : generators) {
            this.profileGeneratorMap.put(generator.getProfileType(), generator);
        }
    }

    /**
     * 根据当前运行状态、场景类型和上报时间生成一次完整的能耗快照。
     */
    public EnergySnapshot generateSnapshot(DeviceRuntimeState runtimeState, SimulatorDeviceProperties deviceProperties,
                                           LocalDateTime reportTime) {
        if (!isSwitchOn(runtimeState)) {
            return buildSkipSnapshot(runtimeState, reportTime);
        }
        EnergyProfileGenerator generator = profileGeneratorMap.get(deviceProperties.getProfileType());
        if (generator == null) {
            throw new IllegalArgumentException("未找到场景类型对应的电量生成器");
        }
        BigDecimal totalIncrement = generator.generateTotalIncrement(reportTime, deviceProperties);
        EnergyIncrement increment = buildPeriodIncrement(resolvePeriod(reportTime), totalIncrement);
        return new EnergySnapshot()
                .setShouldReport(true)
                .setReportTime(reportTime)
                .setIncrement(increment)
                .setTotalEnergy(safe(runtimeState.getLastTotalEnergy()).add(safe(increment.getTotalEnergyIncrement())))
                .setHigherEnergy(safe(runtimeState.getLastHigherEnergy()).add(safe(increment.getHigherEnergyIncrement())))
                .setHighEnergy(safe(runtimeState.getLastHighEnergy()).add(safe(increment.getHighEnergyIncrement())))
                .setLowEnergy(safe(runtimeState.getLastLowEnergy()).add(safe(increment.getLowEnergyIncrement())))
                .setLowerEnergy(safe(runtimeState.getLastLowerEnergy()).add(safe(increment.getLowerEnergyIncrement())))
                .setDeepLowEnergy(safe(runtimeState.getLastDeepLowEnergy()).add(safe(increment.getDeepLowEnergyIncrement())));
    }

    /**
     * 构造“不上报但保留累计值”的快照，通常用于拉闸状态。
     */
    private EnergySnapshot buildSkipSnapshot(DeviceRuntimeState runtimeState, LocalDateTime reportTime) {
        return new EnergySnapshot()
                .setShouldReport(false)
                .setReportTime(reportTime)
                .setIncrement(buildZeroIncrement())
                .setTotalEnergy(safe(runtimeState == null ? null : runtimeState.getLastTotalEnergy()))
                .setHigherEnergy(safe(runtimeState == null ? null : runtimeState.getLastHigherEnergy()))
                .setHighEnergy(safe(runtimeState == null ? null : runtimeState.getLastHighEnergy()))
                .setLowEnergy(safe(runtimeState == null ? null : runtimeState.getLastLowEnergy()))
                .setLowerEnergy(safe(runtimeState == null ? null : runtimeState.getLastLowerEnergy()))
                .setDeepLowEnergy(safe(runtimeState == null ? null : runtimeState.getLastDeepLowEnergy()));
    }

    /**
     * 判断当前设备是否处于允许继续计量和上报的合闸状态。
     */
    private boolean isSwitchOn(DeviceRuntimeState runtimeState) {
        return runtimeState != null && "ON".equalsIgnoreCase(runtimeState.getSwitchStatus());
    }

    /**
     * 根据上报时刻推导当前小时对应的电价时段。
     */
    private ElectricPricePeriodEnum resolvePeriod(LocalDateTime reportTime) {
        int hour = reportTime.getHour();
        if (hour < 6) {
            return ElectricPricePeriodEnum.DEEP_LOW;
        }
        if (hour < 8 || hour >= 22) {
            return ElectricPricePeriodEnum.LOWER;
        }
        if (hour < 11 || (hour >= 13 && hour < 18)) {
            return ElectricPricePeriodEnum.HIGH;
        }
        if (hour < 13) {
            return ElectricPricePeriodEnum.HIGHER;
        }
        return ElectricPricePeriodEnum.LOW;
    }

    /**
     * 将总增量分配到总电量和对应时段电量字段。
     */
    private EnergyIncrement buildPeriodIncrement(ElectricPricePeriodEnum periodEnum, BigDecimal incrementAmount) {
        EnergyIncrement energyIncrement = buildZeroIncrement();
        BigDecimal safeIncrementAmount = incrementAmount == null ? BigDecimal.ZERO : incrementAmount;
        energyIncrement.setTotalEnergyIncrement(safeIncrementAmount);
        if (periodEnum == null) {
            return energyIncrement;
        }
        switch (periodEnum) {
            case HIGHER -> energyIncrement.setHigherEnergyIncrement(safeIncrementAmount);
            case HIGH -> energyIncrement.setHighEnergyIncrement(safeIncrementAmount);
            case LOW -> energyIncrement.setLowEnergyIncrement(safeIncrementAmount);
            case LOWER -> energyIncrement.setLowerEnergyIncrement(safeIncrementAmount);
            case DEEP_LOW -> energyIncrement.setDeepLowEnergyIncrement(safeIncrementAmount);
            default -> {
                return energyIncrement;
            }
        }
        return energyIncrement;
    }

    /**
     * 构造所有分量都为零的增量对象。
     */
    private EnergyIncrement buildZeroIncrement() {
        return new EnergyIncrement()
                .setTotalEnergyIncrement(BigDecimal.ZERO)
                .setHigherEnergyIncrement(BigDecimal.ZERO)
                .setHighEnergyIncrement(BigDecimal.ZERO)
                .setLowEnergyIncrement(BigDecimal.ZERO)
                .setLowerEnergyIncrement(BigDecimal.ZERO)
                .setDeepLowEnergyIncrement(BigDecimal.ZERO);
    }

    /**
     * 统一将空数值兜底为零，避免累计计算出现空指针。
     */
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
