package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.GetDailyEnergyPlanCommand;
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

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

class SfereGetDailyEnergyPlanTranslatorTest {

    @Test
    void testToRequest_FirstPlan_ShouldBuildFirstTimeRequest() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(1, request.getSlaveAddress());
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, request.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME.toMapping().getStartRegister(),
                request.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME.toMapping().getQuantity(),
                request.getQuantity());
    }

    @Test
    void testToRequest_SecondPlan_ShouldBuildSecondTimeRequest() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(2);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(1, request.getSlaveAddress());
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, request.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME_SECOND.toMapping().getStartRegister(),
                request.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_TIME_SECOND.toMapping().getQuantity(),
                request.getQuantity());
    }

    @Test
    void testToRequest_InvalidPlan_ShouldThrow() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(3);

        Assertions.assertThrows(IllegalArgumentException.class, () -> translator.toRequest(command));
    }

    @Test
    void testParseStep_FirstStep_FirstPlan_ShouldReturnFirstPeriodRequest() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] payload = buildReadResponse(new byte[]{0x00, 0x00, 0x06, 0x1E});

        StepResult<ModbusRtuRequest> step = translator.parseStep(command, payload, context);

        Assertions.assertFalse(step.isFinished());
        ModbusRtuRequest nextRequest = step.getNextRequest();
        Assertions.assertNotNull(nextRequest);
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, nextRequest.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD.toMapping().getStartRegister(),
                nextRequest.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD.toMapping().getQuantity(),
                nextRequest.getQuantity());
    }

    @Test
    void testParseStep_FirstStep_SecondPlan_ShouldReturnSecondPeriodRequest() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(2);
        StepContext context = new StepContext();
        byte[] payload = buildReadResponse(new byte[]{0x00, 0x00, 0x06, 0x1E});

        StepResult<ModbusRtuRequest> step = translator.parseStep(command, payload, context);

        Assertions.assertFalse(step.isFinished());
        ModbusRtuRequest nextRequest = step.getNextRequest();
        Assertions.assertNotNull(nextRequest);
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_READ, nextRequest.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD_SECOND.toMapping().getStartRegister(),
                nextRequest.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.DAILY_ENERGY_PLAN_PERIOD_SECOND.toMapping().getQuantity(),
                nextRequest.getQuantity());
    }

    @Test
    void testParseStep_SecondStep_ShouldReturnSlots() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] timePayload = buildReadResponse(new byte[]{0x00, 0x00, 0x06, 0x1E});
        StepResult<ModbusRtuRequest> firstStep = translator.parseStep(command, timePayload, context);
        Assertions.assertFalse(firstStep.isFinished());

        byte[] periodPayload = buildReadResponse(new byte[]{0x00, 0x02});
        StepResult<ModbusRtuRequest> secondStep = translator.parseStep(command, periodPayload, context);

        Assertions.assertTrue(secondStep.isFinished());
        DeviceCommandResult result = secondStep.getResult();
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.getData() instanceof List<?>);
        List<?> slots = (List<?>) result.getData();
        Assertions.assertEquals(2, slots.size());
        DailyEnergySlot first = (DailyEnergySlot) slots.get(0);
        DailyEnergySlot second = (DailyEnergySlot) slots.get(1);
        Assertions.assertEquals(ElectricPricePeriodEnum.HIGHER, first.getPeriod());
        Assertions.assertEquals(LocalTime.of(0, 0), first.getTime());
        Assertions.assertEquals(ElectricPricePeriodEnum.LOW, second.getPeriod());
        Assertions.assertEquals(LocalTime.of(6, 30), second.getTime());
    }

    @Test
    void testParseStep_PeriodLengthShort_ShouldReturnFailure() {
        SfereGetDailyEnergyPlanTranslator translator = new SfereGetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] timePayload = buildReadResponse(new byte[]{0x00, 0x00, 0x06, 0x1E});
        translator.parseStep(command, timePayload, context);

        byte[] periodPayload = buildReadResponse(new byte[]{0x00});
        StepResult<ModbusRtuRequest> step = translator.parseStep(command, periodPayload, context);

        Assertions.assertTrue(step.isFinished());
        DeviceCommandResult result = step.getResult();
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("时段费率数据长度不正确", result.getErrorMessage());
    }

    private DeviceCommand buildCommand(Integer dailyPlanId) {
        Device device = new Device().setSlaveAddress(1);
        GetDailyEnergyPlanCommand payload = new GetDailyEnergyPlanCommand().setDailyPlanId(dailyPlanId);
        return new DeviceCommand()
                .setType(DeviceCommandTypeEnum.GET_DAILY_ENERGY_PLAN)
                .setDevice(device)
                .setPayload(payload);
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
