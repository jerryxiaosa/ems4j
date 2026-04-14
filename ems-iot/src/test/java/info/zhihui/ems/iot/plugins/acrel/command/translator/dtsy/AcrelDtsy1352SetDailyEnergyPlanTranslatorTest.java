package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

class AcrelDtsy1352SetDailyEnergyPlanTranslatorTest {

    @Test
    void toRequest_shouldUseDtsySecondDailyPlanRegister() {
        AcrelDtsy1352SetDailyEnergyPlanTranslator translator = new AcrelDtsy1352SetDailyEnergyPlanTranslator();
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(7))
                .setType(DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN)
                .setPayload(new SetDailyEnergyPlanCommand()
                        .setDailyPlanId(2)
                        .setSlots(List.of(new DailyEnergySlot()
                                .setPeriod(ElectricPricePeriodEnum.HIGH)
                                .setTime(LocalTime.of(8, 30)))));

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(0x2015, request.getStartRegister());
        Assertions.assertEquals(21, request.getQuantity());
        Assertions.assertEquals((byte) ElectricPricePeriodEnum.HIGH.getCode().intValue(), request.getData()[0]);
        Assertions.assertEquals((byte) 30, request.getData()[1]);
        Assertions.assertEquals((byte) 8, request.getData()[2]);
    }
}
