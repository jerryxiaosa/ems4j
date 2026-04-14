package info.zhihui.ems.iot.plugins.acrel.command.constant;

import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * 安科瑞 DTSY 型号寄存器映射常量。
 */
public enum AcrelDtsyRegisterMappingEnum {
    DAILY_ENERGY_PLAN(0x2000, 21),
    DAILY_ENERGY_PLAN_SECOND(0x2015, 21),
    DATE_PLAN(0x0028, 6);

    private final int startRegister;
    private final int quantity;

    AcrelDtsyRegisterMappingEnum(int startRegister, int quantity) {
        this.startRegister = startRegister;
        this.quantity = quantity;
    }

    public ModbusMapping toMapping() {
        return new ModbusMapping()
                .setStartRegister(startRegister)
                .setQuantity(quantity);
    }
}
