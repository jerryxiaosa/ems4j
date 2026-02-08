package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
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

class SfereSetDailyEnergyPlanTranslatorTest {

    @Test
    void type_shouldReturnSetDailyEnergyPlan() {
        SfereSetDailyEnergyPlanTranslator translator = new SfereSetDailyEnergyPlanTranslator();

        Assertions.assertEquals(DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN, translator.type());
    }

    @Test
    void toRequest_shouldUseBackupTimeAndEncodeHourMinute() {
        SfereSetDailyEnergyPlanTranslator translator = new SfereSetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(2);

        ModbusRtuRequest request = translator.toRequest(command);

        Assertions.assertEquals(1, request.getSlaveAddress());
        Assertions.assertEquals(ModbusRtuBuilder.FUNCTION_WRITE, request.getFunction());
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_TIME.toMapping().getStartRegister(),
                request.getStartRegister());
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_TIME.toMapping().getQuantity(),
                request.getQuantity());

        byte[] data = request.getData();
        Assertions.assertNotNull(data);
        Assertions.assertEquals(28, data.length);
        Assertions.assertArrayEquals(new byte[]{0x06, 0x1E, 0x0A, 0x0F}, Arrays.copyOfRange(data, 0, 4));
        for (int i = 4; i < data.length; i++) {
            Assertions.assertEquals(0, data[i]);
        }
    }

    @Test
    void parseStep_shouldFollowSixStepsAndFinish() {
        SfereSetDailyEnergyPlanTranslator translator = new SfereSetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();

        ModbusRtuRequest request1 = translator.toRequest(command);
        StepResult<ModbusRtuRequest> step1 = translator.parseStep(command, buildWriteResponse(request1), context);
        Assertions.assertFalse(step1.isFinished());
        ModbusRtuRequest request2 = step1.getNextRequest();
        Assertions.assertNotNull(request2);
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_PERIOD.toMapping().getStartRegister(),
                request2.getStartRegister());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x04}, Arrays.copyOfRange(request2.getData(), 0, 2));

        StepResult<ModbusRtuRequest> step2 = translator.parseStep(command, buildWriteResponse(request2), context);
        Assertions.assertFalse(step2.isFinished());
        ModbusRtuRequest request3 = step2.getNextRequest();
        Assertions.assertNotNull(request3);
        Assertions.assertEquals(
                SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_TIME_PERIOD_RELATION.toMapping().getStartRegister(),
                request3.getStartRegister());
        Assertions.assertEquals(24, request3.getData().length);
        Assertions.assertTrue(isAllZero(request3.getData()));

        StepResult<ModbusRtuRequest> step3 = translator.parseStep(command, buildWriteResponse(request3), context);
        Assertions.assertFalse(step3.isFinished());
        ModbusRtuRequest request4 = step3.getNextRequest();
        Assertions.assertNotNull(request4);
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_UPDATE_REGISTER.toMapping().getStartRegister(),
                request4.getStartRegister());
        Assertions.assertArrayEquals(new byte[]{0x00, 0x01}, request4.getData());

        StepResult<ModbusRtuRequest> step4 = translator.parseStep(command, buildWriteResponse(request4), context);
        Assertions.assertFalse(step4.isFinished());
        ModbusRtuRequest request5 = step4.getNextRequest();
        Assertions.assertNotNull(request5);
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_CHANGE_DATETIME.toMapping().getStartRegister(),
                request5.getStartRegister());
        Assertions.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00},
                request5.getData());

        StepResult<ModbusRtuRequest> step5 = translator.parseStep(command, buildWriteResponse(request5), context);
        Assertions.assertFalse(step5.isFinished());
        ModbusRtuRequest request6 = step5.getNextRequest();
        Assertions.assertNotNull(request6);
        Assertions.assertEquals(SfereRegisterMappingEnum.BACKUP_CHANGE_PERIOD.toMapping().getStartRegister(),
                request6.getStartRegister());
        Assertions.assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00},
                request6.getData());

        StepResult<ModbusRtuRequest> step6 = translator.parseStep(command, buildWriteResponse(request6), context);
        Assertions.assertTrue(step6.isFinished());
        DeviceCommandResult result = step6.getResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    void parseStep_whenWriteResponseInvalid_shouldReturnFailure() {
        SfereSetDailyEnergyPlanTranslator translator = new SfereSetDailyEnergyPlanTranslator();
        DeviceCommand command = buildCommand(1);
        StepContext context = new StepContext();
        byte[] invalidPayload = new byte[]{0x01, 0x10};

        StepResult<ModbusRtuRequest> step = translator.parseStep(command, invalidPayload, context);

        Assertions.assertTrue(step.isFinished());
        Assertions.assertNotNull(step.getResult());
        Assertions.assertFalse(step.getResult().isSuccess());
    }

    private DeviceCommand buildCommand(Integer dailyPlanId) {
        SetDailyEnergyPlanCommand payload = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(dailyPlanId)
                .setSlots(List.of(
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.HIGHER).setTime(LocalTime.of(6, 30)),
                        new DailyEnergySlot().setPeriod(ElectricPricePeriodEnum.DEEP_LOW).setTime(LocalTime.of(10, 15))
                ));

        return new DeviceCommand()
                .setType(DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN)
                .setDevice(new Device().setSlaveAddress(1))
                .setPayload(payload);
    }

    private byte[] buildWriteResponse(ModbusRtuRequest request) {
        byte[] requestFrame = ModbusRtuBuilder.build(request);
        byte[] body = Arrays.copyOf(requestFrame, 6);
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        frame[body.length] = crc[0];
        frame[body.length + 1] = crc[1];
        return frame;
    }

    private boolean isAllZero(byte[] data) {
        for (byte value : data) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }
}
