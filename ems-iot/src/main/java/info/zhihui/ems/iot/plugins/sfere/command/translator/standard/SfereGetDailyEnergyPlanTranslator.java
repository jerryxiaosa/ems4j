package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.GetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.MultiStepDeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.StepContext;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 斯菲尔 DTSY1946A 获取日时段计划
 */
@Component
public class SfereGetDailyEnergyPlanTranslator extends AbstractSfereCommandTranslator
        implements MultiStepDeviceCommandTranslator<ModbusRtuRequest> {

    private static final String KEY_TIME_LIST = "dailyEnergyPlanTimes";

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        ModbusMapping mapping = resolveTimeMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildRead(mapping, slaveAddress);
    }

    @Override
    public StepResult<ModbusRtuRequest> parseStep(DeviceCommand command, byte[] payload, StepContext context) {
        DeviceCommandResult readResult = parseReadResponse(command, payload, data -> data);
        // 直接返回不成功的数据
        if (!readResult.isSuccess()) {
            return StepResult.done(readResult);
        }
        byte[] data = (byte[]) readResult.getData();
        if (data == null) {
            return StepResult.done(failure(command, "时段计划数据为空", payload));
        }
        List<LocalTime> times = context.get(KEY_TIME_LIST);
        try {
            if (times == null) {
                List<LocalTime> parsedTimes = parseTimes(data);
                context.put(KEY_TIME_LIST, parsedTimes);
                ModbusRtuRequest next = buildPeriodRequest(command);
                return StepResult.next(next);
            }
            List<ElectricPricePeriodEnum> periods = parsePeriods(data, times.size());
            if (periods.size() != times.size()) {
                return StepResult.done(failure(command, "时段数量与费率数量不一致", payload));
            }
            List<DailyEnergySlot> slots = new ArrayList<>(times.size());
            for (int i = 0; i < times.size(); i++) {
                slots.add(new DailyEnergySlot()
                        .setPeriod(periods.get(i))
                        .setTime(times.get(i)));
            }
            return StepResult.done(success(command, slots, payload));
        } catch (RuntimeException ex) {
            return StepResult.done(failure(command, ex.getMessage(), payload));
        }
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN;
    }

    /**
     * 解析时段时间（每两个字节：小时、分钟）。
     */
    private List<LocalTime> parseTimes(byte[] data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("时段时间数据长度不正确");
        }
        List<LocalTime> times = new ArrayList<>(data.length / 2);
        for (int i = 0; i < data.length; i += 2) {
            int hour = Byte.toUnsignedInt(data[i]);
            int minute = Byte.toUnsignedInt(data[i + 1]);
            times.add(LocalTime.of(hour, minute));
        }
        return times;
    }

    /**
     * 解析时段费率号（尖峰平谷编码 0-4）。
     */
    private List<ElectricPricePeriodEnum> parsePeriods(byte[] data, int expectedSize) {
        if (data.length < expectedSize) {
            throw new IllegalArgumentException("时段费率数据长度不正确");
        }
        List<ElectricPricePeriodEnum> periods = new ArrayList<>(expectedSize);
        for (int i = 0; i < data.length && periods.size() < expectedSize; i++) {
            int code = Byte.toUnsignedInt(data[i]);
            periods.add(mapPeriod(code));
        }
        return periods;
    }

    /**
     * 构建时段费率读取请求。
     */
    private ModbusRtuRequest buildPeriodRequest(DeviceCommand command) {
        ModbusMapping mapping = resolvePeriodMapping(command);
        return new ModbusRtuRequest()
                .setSlaveAddress(resolveSlaveAddress(command))
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity());
    }

    private ModbusMapping resolveTimeMapping(DeviceCommand command) {
        Integer dailyPlanId = resolveDailyPlanId(command);
        if (Objects.equals(dailyPlanId, 1)) {
            return SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME.toMapping();
        }
        if (Objects.equals(dailyPlanId, 2)) {
            return SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME_SECOND.toMapping();
        }
        throw new IllegalArgumentException("不支持的方案编号");
    }

    private ModbusMapping resolvePeriodMapping(DeviceCommand command) {
        Integer dailyPlanId = resolveDailyPlanId(command);
        if (Objects.equals(dailyPlanId, 1)) {
            return SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD.toMapping();
        }
        if (Objects.equals(dailyPlanId, 2)) {
            return SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD_SECOND.toMapping();
        }
        throw new IllegalArgumentException("不支持的方案编号");
    }

    private Integer resolveDailyPlanId(DeviceCommand command) {
        if (!(command.getPayload() instanceof GetDailyEnergyPlanCommand payload)) {
            throw new IllegalArgumentException("获取日方案命令参数类型不正确");
        }
        return payload.getDailyPlanId();
    }

    /**
     * 映射尖峰平谷费率号到系统枚举。
     */
    private ElectricPricePeriodEnum mapPeriod(int code) {
        return ElectricPricePeriodEnum.fromCode(code + 1);
    }

}
