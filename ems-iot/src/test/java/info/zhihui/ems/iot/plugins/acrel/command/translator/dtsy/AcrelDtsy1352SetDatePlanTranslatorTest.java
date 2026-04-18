package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.common.model.energy.DatePlanItem;
import info.zhihui.ems.iot.domain.command.concrete.SetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.util.Arrays;
import java.util.List;

class AcrelDtsy1352SetDatePlanTranslatorTest {

    @Test
    void toRequest_shouldUseDtsyDatePlanRegister() {
        AcrelDtsy1352SetDatePlanTranslator translator = new AcrelDtsy1352SetDatePlanTranslator();
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(9))
                .setType(DeviceCommandTypeEnum.SET_DATE_PLAN)
                .setPayload(new SetDatePlanCommand()
                        .setItems(List.of(new DatePlanItem()
                                .setDailyPlanId("1")
                                .setDate(MonthDay.of(3, 2)))));

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(0x0028, request.getStartRegister());
        Assertions.assertEquals(6, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{3, 2, 1}, Arrays.copyOf(request.getData(), 3));
    }
}
