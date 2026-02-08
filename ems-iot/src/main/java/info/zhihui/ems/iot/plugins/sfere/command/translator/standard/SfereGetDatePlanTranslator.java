package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.sfere.command.constant.SfereRegisterMappingEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.protocol.port.outbound.MultiStepDeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.StepContext;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 斯菲尔获取日期方案命令翻译器。
 */
@Component
public class SfereGetDatePlanTranslator extends AbstractSfereCommandTranslator
        implements MultiStepDeviceCommandTranslator<ModbusRtuRequest> {

    private static final int EXPECTED_ITEM_COUNT = 12;
    private static final int BYTES_PER_ITEM = 2;
    private static final int EXPECTED_DATA_LENGTH = EXPECTED_ITEM_COUNT * BYTES_PER_ITEM;
    private static final int STEP_DATE = 0;
    private static final int STEP_RELATION = 1;
    private static final String KEY_STEP_INDEX = "sfereGetDatePlanStep";
    private static final String KEY_DATE_LIST = "sfereGetDatePlanDateList";

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DATE_PLAN;
    }

    @Override
    protected ModbusRtuRequest buildRequest(DeviceCommand command) {
        // 协议当前仅支持日期方案1，忽略 GetDatePlanCommand.plan 入参。
        ModbusMapping mapping = SfereRegisterMappingEnum.DATE_PLAN.toMapping();
        return buildRead(mapping, resolveSlaveAddress(command));
    }

    @Override
    public StepResult<ModbusRtuRequest> parseStep(DeviceCommand command, byte[] payload, StepContext context) {
        DeviceCommandResult readResult = parseReadResponse(command, payload, data -> data);
        if (!readResult.isSuccess()) {
            return StepResult.done(readResult);
        }

        byte[] data = (byte[]) readResult.getData();
        if (data == null) {
            return StepResult.done(failure(command, "日期方案数据为空", payload));
        }

        int stepIndex = resolveStepIndex(context);
        try {
            if (stepIndex == STEP_DATE) {
                List<DateValue> dateValues = parseDateValues(data);
                context.put(KEY_DATE_LIST, dateValues);
                context.put(KEY_STEP_INDEX, STEP_RELATION);
                return StepResult.next(buildRelationRequest(command));
            }
            if (stepIndex == STEP_RELATION) {
                List<Integer> planValues = parsePlanValues(data);
                List<DateValue> dateValues = context.get(KEY_DATE_LIST);
                if (dateValues == null || dateValues.size() != EXPECTED_ITEM_COUNT) {
                    return StepResult.done(failure(command, "日期方案步骤状态异常", payload));
                }
                List<DatePlanItem> mergedItems = mergeAndFilter(dateValues, planValues);
                return StepResult.done(success(command, mergedItems, payload));
            }
            return StepResult.done(failure(command, "日期方案步骤状态异常", payload));
        } catch (RuntimeException ex) {
            return StepResult.done(failure(command, ex.getMessage(), payload));
        }
    }

    private int resolveStepIndex(StepContext context) {
        Integer stepIndex = context.get(KEY_STEP_INDEX);
        return stepIndex == null ? STEP_DATE : stepIndex;
    }

    private ModbusRtuRequest buildRelationRequest(DeviceCommand command) {
        ModbusMapping mapping = SfereRegisterMappingEnum.DATE_PERIOD_RELATION.toMapping();
        return new ModbusRtuRequest()
                .setSlaveAddress(resolveSlaveAddress(command))
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity());
    }

    private List<DateValue> parseDateValues(byte[] data) {
        if (data.length != EXPECTED_DATA_LENGTH) {
            throw new IllegalArgumentException("日期方案数据长度不正确");
        }
        List<DateValue> values = new ArrayList<>(EXPECTED_ITEM_COUNT);
        for (int index = 0; index < EXPECTED_ITEM_COUNT; index++) {
            int base = index * BYTES_PER_ITEM;
            int month = Byte.toUnsignedInt(data[base]);
            int day = Byte.toUnsignedInt(data[base + 1]);
            values.add(new DateValue(month, day));
        }
        return values;
    }

    private List<Integer> parsePlanValues(byte[] data) {
        if (data.length != EXPECTED_DATA_LENGTH) {
            throw new IllegalArgumentException("日期方案时段表关系数据长度不正确");
        }
        List<Integer> values = new ArrayList<>(EXPECTED_ITEM_COUNT);
        for (int index = 0; index < EXPECTED_ITEM_COUNT; index++) {
            int base = index * BYTES_PER_ITEM;
            // 仅使用高字节作为 dailyPlanId，保持原始无符号值。
            int planId = Byte.toUnsignedInt(data[base]);
            values.add(planId);
        }
        return values;
    }

    private List<DatePlanItem> mergeAndFilter(List<DateValue> dateValues, List<Integer> planValues) {
        List<DateValueWithPlan> mergedValues = new ArrayList<>(EXPECTED_ITEM_COUNT);
        for (int index = 0; index < EXPECTED_ITEM_COUNT; index++) {
            DateValue dateValue = dateValues.get(index);
            mergedValues.add(new DateValueWithPlan(dateValue.month(), dateValue.day(), planValues.get(index)));
        }

        List<DatePlanItem> items = new ArrayList<>();
        for (DateValueWithPlan value : mergedValues) {
            if (!isValidDateValue(value.month(), value.day())) {
                continue;
            }
            if (value.planId() <= 0) {
                continue;
            }
            items.add(new DatePlanItem()
                    .setMonth(String.valueOf(value.month()))
                    .setDay(String.valueOf(value.day()))
                    .setDailyPlanId(String.valueOf(value.planId())));
        }
        return items;
    }

    private boolean isValidDateValue(int month, int day) {
        return month >= 1 && month <= 12 && day >= 1 && day <= 31;
    }

    private record DateValue(int month, int day) {
    }

    private record DateValueWithPlan(int month, int day, int planId) {
    }
}
