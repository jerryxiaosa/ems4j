package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

class AcrelGetDailyEnergyPlanTranslatorTest {

    @Test
    void parseResponse_whenPayloadValid_shouldReturnSlotsAndTrimZeros() {
        AcrelGetDailyEnergyPlanTranslator translator = new AcrelGetDailyEnergyPlanTranslator();
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN);
        byte[] data = new byte[]{
                0x04, 0x00, 0x00,
                0x03, 0x00, 0x06,
                0x00, 0x00, 0x00,
                0x00, 0x00, 0x00
        };
        byte[] payload = buildReadResponse(data);

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.getData() instanceof List<?>);
        List<?> list = (List<?>) result.getData();
        Assertions.assertEquals(2, list.size());
        DailyEnergySlot first = (DailyEnergySlot) list.get(0);
        DailyEnergySlot second = (DailyEnergySlot) list.get(1);
        Assertions.assertEquals(ElectricPricePeriodEnum.LOWER, first.getPeriod());
        Assertions.assertEquals(LocalTime.of(0, 0), first.getTime());
        Assertions.assertEquals(ElectricPricePeriodEnum.LOW, second.getPeriod());
        Assertions.assertEquals(LocalTime.of(6, 0), second.getTime());
    }

    @Test
    void parseResponse_whenPayloadLengthInvalid_shouldReturnFailure() {
        AcrelGetDailyEnergyPlanTranslator translator = new AcrelGetDailyEnergyPlanTranslator();
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN);
        byte[] data = new byte[]{0x04, 0x00, 0x00, 0x03};
        byte[] payload = buildReadResponse(data);

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("时段电价返回长度不正确", result.getErrorMessage());
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
}
