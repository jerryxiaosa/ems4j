package info.zhihui.ems.iot.plugins.acrel.command.translator.dtsy;

import info.zhihui.ems.iot.domain.command.concrete.GetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelDtsy1352GetDailyEnergyPlanTranslatorTest {

    @Test
    void toRequest_whenDailyPlanIdIsOne_shouldUseFirstDtsyRegister() {
        AcrelDtsy1352GetDailyEnergyPlanTranslator translator = new AcrelDtsy1352GetDailyEnergyPlanTranslator();
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(3))
                .setType(DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN)
                .setPayload(new GetDailyEnergyPlanCommand().setDailyPlanId(1));

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(0x2000, request.getStartRegister());
        Assertions.assertEquals(21, request.getQuantity());
    }

    @Test
    void toRequest_whenDailyPlanIdIsTwo_shouldUseSecondDtsyRegister() {
        AcrelDtsy1352GetDailyEnergyPlanTranslator translator = new AcrelDtsy1352GetDailyEnergyPlanTranslator();
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(3))
                .setType(DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN)
                .setPayload(new GetDailyEnergyPlanCommand().setDailyPlanId(2));

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(0x2015, request.getStartRegister());
        Assertions.assertEquals(21, request.getQuantity());
    }
}
