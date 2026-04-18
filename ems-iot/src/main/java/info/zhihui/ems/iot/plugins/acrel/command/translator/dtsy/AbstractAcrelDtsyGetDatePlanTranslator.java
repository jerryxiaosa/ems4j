package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelDtsyRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.command.translator.standard.AcrelGetDatePlanTranslator;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * DTSY 日期方案读取命令翻译器基类。
 */
abstract class AbstractAcrelDtsyGetDatePlanTranslator extends AcrelGetDatePlanTranslator {

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return AcrelDtsyRegisterMappingEnum.DATE_PLAN.toMapping();
    }
}
