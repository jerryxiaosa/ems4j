package info.zhihui.ems.iot.plugins.sfere.command.constant;

import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;

/**
 * 斯菲尔寄存器映射常量。
 */
public enum SfereRegisterMappingEnum {
    CT(0x080B, 1),
    CONTROL(0x0B25, 1),
    TOTAL_ENERGY(0x0073, 2),
    HIGHER_ENERGY(0x008B, 2),
    HIGH_ENERGY(0x008D, 2),
    LOW_ENERGY(0x008F, 2),
    LOWER_ENERGY(0x0091, 2),
    DEEP_LOW_ENERGY(0x0093, 2),
    DAILY_ENERGY_PLAN_TIME(0x09A4, 14),
    DAILY_ENERGY_PLAN_PERIOD(0x09F8, 7),
    DAILY_ENERGY_PLAN_TIME_SECOND(0x09B2, 14),
    DAILY_ENERGY_PLAN_PERIOD_SECOND(0x09FF, 7),
    BACKUP_DAILY_ENERGY_PLAN_TIME(0x0A73, 14),
    BACKUP_DAILY_ENERGY_PLAN_PERIOD(0x0AC7, 7),
    // @TODO check
    BACKUP_DAILY_ENERGY_PLAN_TIME_PERIOD_RELATION(0x0AF1, 12),
    BACKUP_UPDATE_REGISTER(0x090C, 1),
    BACKUP_CHANGE_DATETIME(0x0B15, 3),
    BACKUP_CHANGE_PERIOD(0x0B1E, 3),
    DATE_PLAN(0x0A36, 12),
    DATE_PERIOD_RELATION(0x0A22, 12);

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
