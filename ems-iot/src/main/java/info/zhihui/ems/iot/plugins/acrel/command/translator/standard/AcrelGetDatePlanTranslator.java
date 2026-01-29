package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.command.support.AcrelTripleSlotParser;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取日期方案命令翻译器。
 */
@Component
public class AcrelGetDatePlanTranslator extends AbstractAcrelCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DATE_PLAN;
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        return AcrelRegisterMappingEnum.DATE_PLAN.toMapping();
    }

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        ModbusMapping mapping = resolveMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildRead(mapping, slaveAddress);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseReadResponse(command, payload, this::parseDatePlanItems);
    }

    private List<DatePlanItem> parseDatePlanItems(byte[] data) {
        List<AcrelTripleSlotParser.TripleSlot> triples = AcrelTripleSlotParser.parse(
                data, "日期方案返回长度不正确", "日期方案时间不正确");

        List<DatePlanItem> items = new ArrayList<>();
        for (AcrelTripleSlotParser.TripleSlot triple : triples) {
            items.add(new DatePlanItem()
                    .setMonth(String.valueOf(triple.type()))
                    .setDay(String.valueOf(triple.minute()))
                    .setDailyPlanId(String.valueOf(triple.hour())));
        }
        return items;
    }
}
