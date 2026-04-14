package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.domain.command.concrete.GetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelDtsy1352GetDatePlanTranslatorTest {

    @Test
    void toRequest_shouldUseDtsyDatePlanRegister() {
        AcrelDtsy1352GetDatePlanTranslator translator = new AcrelDtsy1352GetDatePlanTranslator();
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(9))
                .setType(DeviceCommandTypeEnum.GET_DATE_PLAN)
                .setPayload(new GetDatePlanCommand());

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(0x0028, request.getStartRegister());
        Assertions.assertEquals(6, request.getQuantity());
    }
}
