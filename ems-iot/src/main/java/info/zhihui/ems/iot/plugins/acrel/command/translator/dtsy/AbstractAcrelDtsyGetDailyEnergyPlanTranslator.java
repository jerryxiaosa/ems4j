package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.domain.command.concrete.GetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelDtsyRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.command.translator.standard.AcrelGetDailyEnergyPlanTranslator;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

import java.util.Objects;

/**
 * DTSY 每日电价方案读取命令翻译器基类。
 */
abstract class AbstractAcrelDtsyGetDailyEnergyPlanTranslator extends AcrelGetDailyEnergyPlanTranslator {

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        GetDailyEnergyPlanCommand payload = (GetDailyEnergyPlanCommand) command.getPayload();
        if (Objects.equals(payload.getDailyPlanId(), 1)) {
            return AcrelDtsyRegisterMappingEnum.DAILY_ENERGY_PLAN.toMapping();
        }
        if (Objects.equals(payload.getDailyPlanId(), 2)) {
            return AcrelDtsyRegisterMappingEnum.DAILY_ENERGY_PLAN_SECOND.toMapping();
        }
        throw new IllegalArgumentException("不支持的方案编号");
    }
}
