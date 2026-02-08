package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.SetDailyEnergyPlanCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.MultiStepDeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.StepContext;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 斯菲尔设置每日电量方案命令翻译器。
 */
@Component
public class SfereSetDailyEnergyPlanTranslator extends AbstractSfereCommandTranslator
        implements MultiStepDeviceCommandTranslator<ModbusRtuRequest> {

    private static final int TOTAL_SLOT_COUNT = 14;
    private static final int TIME_BYTES_PER_SLOT = 2;
    private static final int PERIOD_BYTES_PER_SLOT = 1;
    private static final int STEP_TIME_WRITE = 0;
    private static final int STEP_PERIOD_WRITE = 1;
    private static final int STEP_RELATION_WRITE = 2;
    private static final int STEP_UPDATE_WRITE = 3;
    private static final int STEP_CHANGE_DATETIME_WRITE = 4;
    private static final int STEP_CHANGE_PERIOD_WRITE = 5;
    private static final String KEY_STEP_INDEX = "sfereSetDailyEnergyPlanStep";
    private static final byte[] BACKUP_RELATION_DATA = new byte[24];
    private static final byte[] BACKUP_UPDATE_DATA = new byte[]{0x00, 0x01};
    private static final byte[] BACKUP_CHANGE_MARK_DATA =
            new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00};

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DAILY_ENERGY_PLAN;
    }

    /**
     * 首包固定写入备用时段时间表。
     */
    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        return buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_TIME.toMapping(),
                buildTimeData(command));
    }

    /**
     * 按固定 6 步推进写入流程，每步先校验当前回包，再构造下一步请求。
     */
    @Override
    public StepResult<ModbusRtuRequest> parseStep(DeviceCommand command, byte[] payload, StepContext context) {
        DeviceCommandResult stepResult = parseWriteResponse(command, payload);
        if (!stepResult.isSuccess()) {
            return StepResult.done(stepResult);
        }

        int stepIndex = resolveStepIndex(context);
        return switch (stepIndex) {
            case STEP_TIME_WRITE -> nextStep(context, STEP_PERIOD_WRITE,
                    buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_PERIOD.toMapping(),
                            buildPeriodData(command)));
            case STEP_PERIOD_WRITE -> nextStep(context, STEP_RELATION_WRITE,
                    buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_DAILY_ENERGY_PLAN_TIME_PERIOD_RELATION.toMapping(),
                            BACKUP_RELATION_DATA));
            case STEP_RELATION_WRITE -> nextStep(context, STEP_UPDATE_WRITE,
                    buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_UPDATE_REGISTER.toMapping(),
                            BACKUP_UPDATE_DATA));
            case STEP_UPDATE_WRITE -> nextStep(context, STEP_CHANGE_DATETIME_WRITE,
                    buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_CHANGE_DATETIME.toMapping(),
                            BACKUP_CHANGE_MARK_DATA));
            case STEP_CHANGE_DATETIME_WRITE -> nextStep(context, STEP_CHANGE_PERIOD_WRITE,
                    buildWriteRequest(command, SfereRegisterMappingEnum.BACKUP_CHANGE_PERIOD.toMapping(),
                            BACKUP_CHANGE_MARK_DATA));
            case STEP_CHANGE_PERIOD_WRITE -> StepResult.done(stepResult);
            default -> StepResult.done(failure(command, "设置日方案步骤状态异常", payload));
        };
    }

    /**
     * 记录下一步编号并返回下一步请求。
     */
    private StepResult<ModbusRtuRequest> nextStep(StepContext context, int nextIndex, ModbusRtuRequest request) {
        context.put(KEY_STEP_INDEX, nextIndex);
        return StepResult.next(request);
    }

    /**
     * 首次进入步骤上下文时默认处于第 0 步（写时间）。
     */
    private int resolveStepIndex(StepContext context) {
        Integer value = context.get(KEY_STEP_INDEX);
        return value == null ? STEP_TIME_WRITE : value;
    }

    /**
     * 统一封装写寄存器请求构造。
     */
    private ModbusRtuRequest buildWriteRequest(DeviceCommand command, ModbusMapping mapping, byte[] data) {
        int slaveAddress = resolveSlaveAddress(command);
        return buildWrite(mapping, slaveAddress, data);
    }

    /**
     * 时间编码：每个时段 2 字节，高字节小时、低字节分钟；不足 14 组补 0。
     */
    private byte[] buildTimeData(DeviceCommand command) {
        SetDailyEnergyPlanCommand payload = resolvePayload(command);
        List<DailyEnergySlot> slots = payload.getSlots();
        byte[] data = new byte[TOTAL_SLOT_COUNT * TIME_BYTES_PER_SLOT];
        int index = 0;
        for (DailyEnergySlot slot : slots) {
            if (index >= data.length) {
                break;
            }
            data[index++] = (byte) slot.getTime().getHour();
            data[index++] = (byte) slot.getTime().getMinute();
        }
        return data;
    }

    /**
     * 费率编码：每个时段 1 字节，写入值为 periodCode - 1；不足 14 组补 0。
     */
    private byte[] buildPeriodData(DeviceCommand command) {
        SetDailyEnergyPlanCommand payload = resolvePayload(command);
        List<DailyEnergySlot> slots = payload.getSlots();
        byte[] data = new byte[TOTAL_SLOT_COUNT * PERIOD_BYTES_PER_SLOT];
        int index = 0;
        for (DailyEnergySlot slot : slots) {
            if (index >= data.length) {
                break;
            }
            int periodCode = slot.getPeriod().getCode();
            data[index++] = (byte) (periodCode - 1);
        }
        return data;
    }

    /**
     * 解析并校验命令负载类型。
     */
    private SetDailyEnergyPlanCommand resolvePayload(DeviceCommand command) {
        if (!(command.getPayload() instanceof SetDailyEnergyPlanCommand payload)) {
            throw new IllegalArgumentException("设置日方案命令参数类型不正确");
        }
        return payload;
    }
}
