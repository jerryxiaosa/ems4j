package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.command.support.AcrelTripleSlotParser;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取每日电量方案命令翻译器。
 */
@Component
public class AcrelGetDailyEnergyPlanTranslator extends AbstractAcrelCommandTranslator {

    public AcrelGetDailyEnergyPlanTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        ModbusMapping mapping = requireMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        return buildRead(mapping, slaveAddress);
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseReadResponse(command, payload, this::parseDailySlots);
    }

    private List<DailyEnergySlot> parseDailySlots(byte[] data) {
        List<AcrelTripleSlotParser.TripleSlot> triples = AcrelTripleSlotParser.parse(
                data, "时段电价返回长度不正确", "时段电价时间不正确");

        List<DailyEnergySlot> slots = new ArrayList<>();
        for (AcrelTripleSlotParser.TripleSlot triple : triples) {
            ElectricPricePeriodEnum period = ElectricPricePeriodEnum.fromCode(triple.type());
            slots.add(new DailyEnergySlot()
                    .setPeriod(period)
                    .setTime(LocalTime.of(triple.hour(), triple.minute())));
        }
        return slots;
    }
}
