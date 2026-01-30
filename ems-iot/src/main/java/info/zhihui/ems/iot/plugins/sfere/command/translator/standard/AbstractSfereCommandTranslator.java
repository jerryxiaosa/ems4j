package info.zhihui.ems.iot.plugins.sfere.command.translator.standard;

import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractModbusCommandTranslator;

/**
 * 斯菲尔命令翻译器基础能力。
 */
public abstract class AbstractSfereCommandTranslator extends AbstractModbusCommandTranslator {

    protected AbstractSfereCommandTranslator() {
    }

    @Override
    public String vendor() {
        return VendorEnum.SFERE.name();
    }
}
