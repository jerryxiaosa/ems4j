package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.domain.command.concrete.SetCtCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelSetCtTranslatorTest {

    @Test
    void type_shouldReturnSetCt() {
        AcrelSetCtTranslator translator = new AcrelSetCtTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.SET_CT, translator.type());
    }

    @Test
    void toRequest_WithValidCt_ShouldBuildWriteRequest() {
        AcrelSetCtTranslator translator = new AcrelSetCtTranslator();
        Device device = new Device().setSlaveAddress(2);
        SetCtCommand payload = new SetCtCommand().setCt(300);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.SET_CT)
                .setPayload(payload);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(2, request.getSlaveAddress());
        Assertions.assertEquals(0x10, request.getFunction());
        Assertions.assertEquals(0x0038, request.getStartRegister());
        Assertions.assertEquals(1, request.getQuantity());
        Assertions.assertArrayEquals(new byte[]{0x01, 0x2C}, request.getData());
    }

    @Test
    void toRequest_WhenCtOutOfRange_ShouldThrow() {
        AcrelSetCtTranslator translator = new AcrelSetCtTranslator();
        Device device = new Device().setSlaveAddress(1);
        SetCtCommand payload = new SetCtCommand().setCt(0xFFFF_0000);
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.SET_CT)
                .setPayload(payload);

        Assertions.assertThrows(BusinessRuntimeException.class, () -> translator.toRequest(command));
    }
}
