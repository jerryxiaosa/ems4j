package info.zhihui.ems.iot.plugins.sfere.command.constant;

import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * 斯菲尔寄存器映射常量。
 */
public enum SfereRegisterMappingEnum {
    CT(0x0B25, 1),
    CONTROL(0x0B25, 1),
    TOTAL_ENERGY(0x0073, 2),
    HIGHER_ENERGY(0x008B, 2),
    HIGH_ENERGY(0x008D, 2),
    LOW_ENERGY(0x008F, 2),
    LOWER_ENERGY(0x0091, 2),
    DEEP_LOW_ENERGY(0x0093, 2),
    DAILY_ENERGY_PLAN_TIME(0x09A4, 14),
    DAILY_ENERGY_PLAN_PERIOD(0x09F8, 7),
    DATE_PLAN(0x2000, 6);

    private final int startRegister;
    private final int quantity;

    SfereRegisterMappingEnum(int startRegister, int quantity) {
        this.startRegister = startRegister;
        this.quantity = quantity;
    }

    public ModbusMapping toMapping() {
        return new ModbusMapping()
                .setStartRegister(startRegister)
                .setQuantity(quantity);
    }
}
