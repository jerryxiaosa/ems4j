package info.zhihui.ems.iot.enums;

/**
 * 统一的设备命令类型定义，便于不同厂商实现共享。
 */
public enum DeviceCommandTypeEnum {
    CUT_OFF,
    RECOVER,
    SET_CT,
    GET_CT,
    SET_DATE_PLAN,
    GET_DATE_PLAN,
    SET_DAILY_ENERGY_PLAN,
    GET_DAILY_ENERGY_PLAN,
    GET_TOTAL_ENERGY,
    GET_HIGHER_ENERGY,
    GET_HIGH_ENERGY,
    GET_LOW_ENERGY,
    GET_LOWER_ENERGY,
    GET_DEEP_LOW_ENERGY
}
