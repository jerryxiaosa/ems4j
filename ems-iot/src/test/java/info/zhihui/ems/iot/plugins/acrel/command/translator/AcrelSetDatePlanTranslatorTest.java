package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.command.concrete.SetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

class AcrelSetDatePlanTranslatorTest {

    @Test
    void toRequest_shouldBuildDataAndPadZeros() {
        AcrelSetDatePlanTranslator translator = new AcrelSetDatePlanTranslator(new AcrelModbusMappingRegistry());
        SetDatePlanCommand payload = new SetDatePlanCommand()
                .setPlan(1)
                .setItems(List.of(
                        new DatePlanItem().setPlan("1").setDay("2").setMonth("3"),
                        new DatePlanItem().setPlan("4").setDay("5").setMonth("6")
                ));
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(2))
                .setType(DeviceCommandTypeEnum.SET_DATE_PLAN)
                .setPayload(payload);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(2, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x2000, request.getStartRegister());
        Assertions.assertEquals(6, request.getQuantity());
        byte[] data = request.getData();
        Assertions.assertEquals(12, data.length);
        Assertions.assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, Arrays.copyOf(data, 6));
        for (int i = 6; i < data.length; i++) {
            Assertions.assertEquals(0, data[i]);
        }
    }

    @Test
    void toRequest_whenItemsTooMany_shouldThrow() {
        AcrelSetDatePlanTranslator translator = new AcrelSetDatePlanTranslator(new AcrelModbusMappingRegistry());
        List<DatePlanItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            items.add(new DatePlanItem().setPlan("1").setDay("1").setMonth("1"));
        }
        SetDatePlanCommand payload = new SetDatePlanCommand()
                .setPlan(1)
                .setItems(items);
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(1))
                .setType(DeviceCommandTypeEnum.SET_DATE_PLAN)
                .setPayload(payload);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> translator.toRequest(command));

        Assertions.assertEquals("日期方案最多支持 4 组", ex.getMessage());
    }

    @Test
    void toRequest_whenItemInvalid_shouldThrow() {
        AcrelSetDatePlanTranslator translator = new AcrelSetDatePlanTranslator(new AcrelModbusMappingRegistry());
        SetDatePlanCommand payload = new SetDatePlanCommand()
                .setPlan(1)
                .setItems(List.of(new DatePlanItem().setPlan("bad").setDay("1").setMonth("1")));
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(1))
                .setType(DeviceCommandTypeEnum.SET_DATE_PLAN)
                .setPayload(payload);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> translator.toRequest(command));

        Assertions.assertEquals("日期方案类型不正确", ex.getMessage());
    }
}
