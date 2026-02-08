package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.command.concrete.GetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.StepContext;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class SfereGetDatePlanTranslatorTest {

    @Test
    void type_shouldReturnGetDatePlan() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.GET_DATE_PLAN, translator.type());
    }

    @Test
    void toRequest_shouldUseDatePlanMappingAndIgnorePlanParameter() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(99);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(1, request.getSlaveAddress());
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, request.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DATE_PLAN.toMapping().getStartRegister(),
                request.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DATE_PLAN.toMapping().getQuantity(),
                request.getQuantity());
    }

    @Test
    void parseStep_firstStep_shouldReturnDatePeriodRelationRequest() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(2);
        StepContext context = new StepContext();
        byte[] datePayload = buildReadResponse(buildDateData());

        StepResult<ModbusRtuRequest> step = translator.parseStep(command, datePayload, context);

        Assertions.assertFalse(step.isFinished());
        ModbusRtuRequest nextRequest = step.getNextRequest();
        Assertions.assertNotNull(nextRequest);
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, nextRequest.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DATE_PERIOD_RELATION.toMapping().getStartRegister(),
                nextRequest.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DATE_PERIOD_RELATION.toMapping().getQuantity(),
                nextRequest.getQuantity());
    }

    @Test
    void parseStep_secondStep_shouldMergeByIndexAndFilterInvalidItems() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] datePayload = buildReadResponse(buildDateData());
        StepResult<ModbusRtuRequest> firstStep = translator.parseStep(command, datePayload, context);
        Assertions.assertFalse(firstStep.isFinished());

        byte[] relationPayload = buildReadResponse(buildRelationData());
        StepResult<ModbusRtuRequest> secondStep = translator.parseStep(command, relationPayload, context);

        Assertions.assertTrue(secondStep.isFinished());
        DeviceCommandResult result = secondStep.getResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.getData() instanceof List<?>);

        List<?> items = (List<?>) result.getData();
        Assertions.assertEquals(2, items.size());

        DatePlanItem first = (DatePlanItem) items.get(0);
        Assertions.assertEquals("1", first.getMonth());
        Assertions.assertEquals("2", first.getDay());
        Assertions.assertEquals("1", first.getDailyPlanId());

        DatePlanItem second = (DatePlanItem) items.get(1);
        Assertions.assertEquals("12", second.getMonth());
        Assertions.assertEquals("31", second.getDay());
        Assertions.assertEquals("5", second.getDailyPlanId());
    }

    @Test
    void parseStep_secondStep_shouldUseRelationHighByteRawUnsignedValue() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(999);
        StepContext context = new StepContext();
        byte[] datePayload = buildReadResponse(buildDateData());
        StepResult<ModbusRtuRequest> firstStep = translator.parseStep(command, datePayload, context);
        Assertions.assertFalse(firstStep.isFinished());

        byte[] relationPayload = buildReadResponse(buildRelationDataWithRawHighByte());
        StepResult<ModbusRtuRequest> secondStep = translator.parseStep(command, relationPayload, context);

        Assertions.assertTrue(secondStep.isFinished());
        Assertions.assertNotNull(secondStep.getResult());
        Assertions.assertTrue(secondStep.getResult().isSuccess());
        List<?> items = (List<?>) secondStep.getResult().getData();
        Assertions.assertFalse(items.isEmpty());

        DatePlanItem first = (DatePlanItem) items.get(0);
        Assertions.assertEquals("254", first.getDailyPlanId());
    }

    @Test
    void parseStep_whenDatePayloadLengthInvalid_shouldReturnFailure() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] payload = buildReadResponse(new byte[]{0x01, 0x02});

        StepResult<ModbusRtuRequest> step = translator.parseStep(command, payload, context);

        Assertions.assertTrue(step.isFinished());
        Assertions.assertNotNull(step.getResult());
        Assertions.assertFalse(step.getResult().isSuccess());
        Assertions.assertEquals("日期方案数据长度不正确", step.getResult().getErrorMessage());
    }

    @Test
    void parseStep_whenRelationPayloadLengthInvalid_shouldReturnFailure() {
        SfereGetDatePlanTranslator translator = new SfereGetDatePlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] datePayload = buildReadResponse(buildDateData());
        StepResult<ModbusRtuRequest> firstStep = translator.parseStep(command, datePayload, context);
        Assertions.assertFalse(firstStep.isFinished());

        byte[] relationPayload = buildReadResponse(new byte[]{0x01, 0x00});
        StepResult<ModbusRtuRequest> secondStep = translator.parseStep(command, relationPayload, context);

        Assertions.assertTrue(secondStep.isFinished());
        Assertions.assertNotNull(secondStep.getResult());
        Assertions.assertFalse(secondStep.getResult().isSuccess());
        Assertions.assertEquals("日期方案时段表关系数据长度不正确", secondStep.getResult().getErrorMessage());
    }

    private DeviceCommand buildCommand(Integer plan) {
        GetDatePlanCommand payload = new GetDatePlanCommand().setPlan(plan);
        return new DeviceCommand()
                .setType(DeviceCommandTypeEnum.GET_DATE_PLAN)
                .setDevice(new Device().setSlaveAddress(1))
                .setPayload(payload);
    }

    private byte[] buildDateData() {
        byte[] data = new byte[24];
        data[0] = 0x01;
        data[1] = 0x02;
        data[2] = 0x00;
        data[3] = 0x00;
        data[4] = 0x0D;
        data[5] = 0x01;
        data[6] = 0x02;
        data[7] = 0x1E;
        data[8] = 0x0C;
        data[9] = 0x1F;
        return data;
    }

    private byte[] buildRelationData() {
        byte[] data = new byte[24];
        data[0] = 0x01;
        data[1] = 0x02;
        data[2] = 0x01;
        data[3] = 0x02;
        data[4] = 0x02;
        data[5] = 0x03;
        data[6] = 0x00;
        data[7] = 0x04;
        data[8] = 0x05;
        data[9] = 0x06;
        return data;
    }

    private byte[] buildRelationDataWithRawHighByte() {
        byte[] data = buildRelationData();
        data[0] = (byte) 0xFE;
        data[1] = 0x00;
        return data;
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
