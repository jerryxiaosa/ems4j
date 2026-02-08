package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class SetDailyEnergyPlanCommandTest {

    @Test
    void validate_whenValid_shouldPass() {
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(List.of(
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.HIGHER).setTime(LocalTime.of(6, 30)),
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.DEEP_LOW).setTime(LocalTime.of(10, 15))
                ));

        Assertions.assertDoesNotThrow(command::validate);
    }

    @Test
    void validate_whenDailyPlanIdOutOfRange_shouldThrow() {
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(3)
                .setSlots(List.of(new DailyEnergySlot()
                        .setPeriod(ElectricPricePeriodEnum.HIGHER)
                        .setTime(LocalTime.of(6, 30))));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, command::validate);
        Assertions.assertEquals("日方案编号范围为1~2", ex.getMessage());
    }

    @Test
    void validate_whenTotalPeriod_shouldThrow() {
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(List.of(new DailyEnergySlot()
                        .setPeriod(ElectricPricePeriodEnum.TOTAL)
                        .setTime(LocalTime.of(6, 30))));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, command::validate);
        Assertions.assertEquals("时段费率不支持总电价", ex.getMessage());
    }

    @Test
    void validate_whenTimeNotAscending_shouldThrow() {
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(List.of(
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.HIGHER).setTime(LocalTime.of(10, 30)),
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.LOW).setTime(LocalTime.of(6, 30))
                ));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, command::validate);
        Assertions.assertEquals("时段时间必须升序且不能重复", ex.getMessage());
    }

    @Test
    void validate_whenTimeDuplicate_shouldThrow() {
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(List.of(
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.HIGHER).setTime(LocalTime.of(6, 30)),
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.LOW).setTime(LocalTime.of(6, 30))
                ));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, command::validate);
        Assertions.assertEquals("时段时间必须升序且不能重复", ex.getMessage());
    }

    @Test
    void validate_whenSlotCountExceedsLimit_shouldThrow() {
        List<DailyEnergySlot> slots = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            slots.add(new DailyEnergySlot()
                    .setPeriod(ElectricPricePeriodEnum.HIGHER)
                    .setTime(LocalTime.of(i, 0)));
        }
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(1)
                .setSlots(slots);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, command::validate);
        Assertions.assertEquals("时段配置最多支持14组", ex.getMessage());
    }
}
