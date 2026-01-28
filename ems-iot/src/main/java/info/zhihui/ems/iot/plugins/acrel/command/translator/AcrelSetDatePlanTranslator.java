package info.zhihui.ems.iot.plugins.acrel.command.translator;

import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.command.concrete.SetDatePlanCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.outbound.modbus.AcrelModbusMappingRegistry;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.springframework.stereotype.Component;

/**
 * 设置日期方案命令翻译器。
 */
@Component
public class AcrelSetDatePlanTranslator extends AbstractAcrelCommandTranslator {

    public AcrelSetDatePlanTranslator(AcrelModbusMappingRegistry mappingRegistry) {
        super(mappingRegistry);
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DATE_PLAN;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        SetDatePlanCommand payload = (SetDatePlanCommand) command.getPayload();
        payload.validate();

        int slaveAddress = resolveSlaveAddress(command);
        ModbusMapping mapping = requireMapping(command);
        byte[] data = buildData(payload);

        return buildWrite(mapping, slaveAddress, data);
    }

    private byte[] buildData(SetDatePlanCommand payload) {
        if (payload == null || payload.getItems() == null) {
            throw new IllegalArgumentException("日期方案配置不能为空");
        }
        // 12 / 3 = 4
        int maxItems = 4;
        if (payload.getItems().size() > maxItems) {
            throw new IllegalArgumentException("日期方案最多支持 4 组");
        }
        byte[] data = new byte[maxItems * 3];
        int index = 0;
        for (DatePlanItem item : payload.getItems()) {
            if (item == null) {
                throw new IllegalArgumentException("日期方案配置不完整");
            }
            int plan = parseByteValue(item.getPlan(), "日期方案类型不正确");
            int day = parseByteValue(item.getDay(), "日期方案日期不正确");
            int month = parseByteValue(item.getMonth(), "日期方案月份不正确");
            data[index++] = (byte) plan;
            data[index++] = (byte) day;
            data[index++] = (byte) month;
        }
        return data;
    }

    private int parseByteValue(String value, String error) {
        if (value == null) {
            throw new IllegalArgumentException(error);
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0 || parsed > 0xFF) {
                throw new IllegalArgumentException(error);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(error);
        }
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        return parseWriteResponse(command, payload);
    }
}
