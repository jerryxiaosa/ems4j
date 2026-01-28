package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class AbstractAcrelEnergyTranslatorTest {

    @Test
    void toRequest_shouldBuildReadRequest() {
        TestEnergyTranslator translator = new TestEnergyTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand()
                .setDevice(new Device().setSlaveAddress(5))
                .setType(DeviceCommandTypeEnum.GET_TOTAL_ENERGY);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(5, request.getSlaveAddress());
        Assertions.assertEquals(0x03, request.getFunction());
        Assertions.assertEquals(0x0000, request.getStartRegister());
        Assertions.assertEquals(2, request.getQuantity());
    }

    @Test
    void parseResponse_whenDataValid_shouldReturnValue() {
        TestEnergyTranslator translator = new TestEnergyTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_TOTAL_ENERGY);
        byte[] payload = buildReadResponse(new byte[]{0x00, 0x00, 0x01, 0x02});

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(258, result.getData());
    }

    @Test
    void parseResponse_whenDataTooShort_shouldReturnFailure() {
        TestEnergyTranslator translator = new TestEnergyTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_TOTAL_ENERGY);
        byte[] payload = buildReadResponse(new byte[]{0x01, 0x02});

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("电量数据长度不足", result.getErrorMessage());
    }

    private byte[] buildReadResponse(byte[] data) {
        byte[] body = new byte[3 + data.length];
        body[0] = 0x01;
        body[1] = 0x03;
        body[2] = (byte) data.length;
        System.arraycopy(data, 0, body, 3, data.length);
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }

    private static class TestEnergyTranslator extends AbstractAcrelEnergyTranslator {

        private TestEnergyTranslator(AcrelModbusMappingRegistry mappingRegistry) {
            super(mappingRegistry);
        }

        @Override
        public DeviceCommandTypeEnum type() {
            return DeviceCommandTypeEnum.GET_TOTAL_ENERGY;
        }
    }
}
