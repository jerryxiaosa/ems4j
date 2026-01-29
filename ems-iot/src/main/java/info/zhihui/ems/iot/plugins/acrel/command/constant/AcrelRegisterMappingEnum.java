package info.zhihui.ems.iot.plugins.acrel.command.constant;

import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * 安科瑞寄存器映射常量。
 */
public enum AcrelRegisterMappingEnum {
    CT(0x0038, 1),
    CONTROL(0x0057, 2),
    TOTAL_ENERGY(0x0000, 2),
    HIGHER_ENERGY(0x0002, 2),
    HIGH_ENERGY(0x0004, 2),
    LOW_ENERGY(0x0006, 2),
    LOWER_ENERGY(0x0008, 2),
    DEEP_LOW_ENERGY(0x5030, 2),
    DAILY_ENERGY_PLAN(0x2006, 21),
    DAILY_ENERGY_PLAN_SECOND(0x201B, 21),
    DATE_PLAN(0x2000, 6);

    private final int startRegister;
    private final int quantity;

    AcrelRegisterMappingEnum(int startRegister, int quantity) {
        this.startRegister = startRegister;
        this.quantity = quantity;
    }

    public ModbusMapping toMapping() {
        return new ModbusMapping()
                .setStartRegister(startRegister)
                .setQuantity(quantity);
    }
}
