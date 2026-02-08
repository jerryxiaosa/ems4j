package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class AcrelSetDailyEnergyPlanTranslatorTest {

    @Test
    void toRequest_shouldBuildDataAndPadZeros() {
        AcrelSetDailyEnergyPlanTranslator translator = new AcrelSetDailyEnergyPlanTranslator();
        SetDailyEnergyPlanCommand payload = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(List.of(
                        new DailyEnergySlot()
                                .setPeriod(ElectricPricePeriodEnum.HIGHER)
                                .setTime(LocalTime.of(6, 30)),
                        new DailyEnergySlot()
                                .setPeriod(ElectricPricePeriodEnum.LOW)
                                .setTime(LocalTime.of(23, 59))
                ));
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(7))
                .setType(DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN)
                .setPayload(payload);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(7, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x2006, request.getStartRegister());
        Assertions.assertEquals(21, request.getQuantity());
        byte[] data = request.getData();
        Assertions.assertEquals(42, data.length);
        Assertions.assertEquals((byte) ElectricPricePeriodEnum.HIGHER.getCode().intValue(), data[0]);
        Assertions.assertEquals((byte) 30, data[1]);
        Assertions.assertEquals((byte) 6, data[2]);
        Assertions.assertEquals((byte) ElectricPricePeriodEnum.LOW.getCode().intValue(), data[3]);
        Assertions.assertEquals((byte) 59, data[4]);
        Assertions.assertEquals((byte) 23, data[5]);
        for (int i = 6; i < data.length; i++) {
            Assertions.assertEquals(0, data[i]);
        }
    }

    @Test
    void toRequest_whenSlotsTooMany_shouldThrow() {
        AcrelSetDailyEnergyPlanTranslator translator = new AcrelSetDailyEnergyPlanTranslator();
        List<DailyEnergySlot> slots = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            slots.add(new DailyEnergySlot()
                    .setPeriod(ElectricPricePeriodEnum.HIGHER)
                    .setTime(LocalTime.of(0, 0)));
        }
        SetDailyEnergyPlanCommand payload = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(slots);
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(1))
                .setType(DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN)
                .setPayload(payload);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> translator.toRequest(command));

        Assertions.assertEquals("时段配置最多支持14组", ex.getMessage());
    }
}
