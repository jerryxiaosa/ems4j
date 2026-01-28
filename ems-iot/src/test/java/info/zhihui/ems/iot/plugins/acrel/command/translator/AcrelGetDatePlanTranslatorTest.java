package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class AcrelGetDatePlanTranslatorTest {

    @Test
    void parseResponse_whenPayloadValid_shouldReturnItems() {
        AcrelGetDatePlanTranslator translator = new AcrelGetDatePlanTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_DATE_PLAN);
        byte[] data = new byte[]{
                0x01, 0x02, 0x03,
                0x04, 0x05, 0x06,
                0x00, 0x00, 0x00
        };
        byte[] payload = buildReadResponse(data);

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.getData() instanceof List<?>);
        List<?> list = (List<?>) result.getData();
        Assertions.assertEquals(2, list.size());
        DatePlanItem first = (DatePlanItem) list.get(0);
        DatePlanItem second = (DatePlanItem) list.get(1);
        Assertions.assertEquals("1", first.getMonth());
        Assertions.assertEquals("2", first.getDay());
        Assertions.assertEquals("3", first.getPlan());
        Assertions.assertEquals("4", second.getMonth());
        Assertions.assertEquals("5", second.getDay());
        Assertions.assertEquals("6", second.getPlan());
    }

    @Test
    void parseResponse_whenPayloadLengthInvalid_shouldReturnFailure() {
        AcrelGetDatePlanTranslator translator = new AcrelGetDatePlanTranslator(new AcrelModbusMappingRegistry());
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_DATE_PLAN);
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};
        byte[] payload = buildReadResponse(data);

        DeviceCommandResult result = translator.parseResponse(command, payload);

        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("日期方案返回长度不正确", result.getErrorMessage());
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
