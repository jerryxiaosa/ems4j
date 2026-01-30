package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractModbusCommandTranslator;

/**
 * 安科瑞命令翻译器基础能力。
 */
public abstract class AbstractAcrelCommandTranslator extends AbstractModbusCommandTranslator {

    protected AbstractAcrelCommandTranslator() {
    }

    /**
     * 获取命令对应的 Modbus 地址映射。
     */
    protected abstract ModbusMapping resolveMapping(DeviceCommand command);

    /**
     * 返回当前翻译器的厂商标识。
     */
    @Override
    public String vendor() {
        return VendorEnum.ACREL.name();
    }
}
