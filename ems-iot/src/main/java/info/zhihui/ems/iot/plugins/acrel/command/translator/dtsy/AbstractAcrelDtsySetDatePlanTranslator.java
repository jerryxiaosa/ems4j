package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelDtsyRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.command.translator.standard.AcrelSetDatePlanTranslator;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * DTSY 日期方案下发命令翻译器基类。
 */
abstract class AbstractAcrelDtsySetDatePlanTranslator extends AcrelSetDatePlanTranslator {

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return AcrelDtsyRegisterMappingEnum.DATE_PLAN.toMapping();
    }
}
