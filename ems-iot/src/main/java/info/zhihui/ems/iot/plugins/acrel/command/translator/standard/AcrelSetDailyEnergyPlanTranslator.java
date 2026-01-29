package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.GetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 设置每日电量方案命令翻译器。
 */
@Component
public class AcrelSetDailyEnergyPlanTranslator extends AbstractAcrelCommandTranslator {

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN;
    }

    @Override
    protected ModbusMapping resolveMapping(DeviceCommand command) {
        SetDailyEnergyPlanCommand payload = (SetDailyEnergyPlanCommand) command.getPayload();
        if (Objects.equals(payload.getDailyPlanId(), 1)) {
            return AcrelRegisterMappingEnum.DAILY_ENERGY_PLAN.toMapping();
        } else if (Objects.equals(payload.getDailyPlanId(), 2)) {
            return AcrelRegisterMappingEnum.DAILY_ENERGY_PLAN_SECOND.toMapping();
        } else {
            throw new IllegalArgumentException("不支持的方案编号");
        }
    }

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        SetDailyEnergyPlanCommand payload = (SetDailyEnergyPlanCommand) command.getPayload();
        ModbusMapping mapping = resolveMapping(command);
        int slaveAddress = resolveSlaveAddress(command);
        byte[] data = buildData(payload);

        return buildWrite(mapping, slaveAddress, data);
    }

    private byte[] buildData(SetDailyEnergyPlanCommand payload) {
        if (payload == null || payload.getSlots() == null) {
            throw new IllegalArgumentException("时段电价配置不能为空");
        }
        // 42 / 3 = 14
        int maxSlots = 14;
        if (payload.getSlots().size() > maxSlots) {
            throw new IllegalArgumentException("时段电价配置最多支持 14 组");
        }
        byte[] data = new byte[maxSlots * 3];
        int index = 0;
        for (DailyEnergySlot slot : payload.getSlots()) {
            if (slot == null || slot.getPeriod() == null || slot.getTime() == null) {
                throw new IllegalArgumentException("时段电价配置不完整");
            }
            int plan = slot.getPeriod().getCode();
            int minute = slot.getTime().getMinute();
            int hour = slot.getTime().getHour();
            if (plan < 0 || plan > 0xFF) {
                throw new IllegalArgumentException("时段电价类型不正确");
            }

            data[index++] = (byte) plan;
            data[index++] = (byte) minute;
            data[index++] = (byte) hour;
        }
        return data;
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }
}
