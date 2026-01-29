package info.zhihui.ems.iot.plugins.acrel.command.translator.standard;

import info.zhihui.ems.iot.plugins.acrel.constant.AcrelPluginConstants;
import info.zhihui.ems.iot.protocol.port.outbound.AbstractModbusCommandTranslator;

/**
 * 安科瑞命令翻译器基础能力。
 */
public abstract class AbstractAcrelCommandTranslator extends AbstractModbusCommandTranslator {

    protected AbstractAcrelCommandTranslator() {
    }

    /**
     * 返回当前翻译器的厂商标识。
     */
    @Override
    public String vendor() {
        return AcrelPluginConstants.VENDOR;
    }
}
