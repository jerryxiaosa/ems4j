package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant;

/**
 * 安科瑞 4G 直连 payload 常量。
 */
public final class Acrel4gPayloadConstants {

    private Acrel4gPayloadConstants() {
    }

    /**
     * 注册报文中的设备序列号固定长度。
     */
    public static final int SERIAL_NUMBER_LENGTH = 20;

    /**
     * 注册报文中的 ICCID 固定长度。
     */
    public static final int ICCID_LENGTH = 30;

    /**
     * 注册报文内 RSSI 字段偏移。
     */
    public static final int REGISTER_RSSI_OFFSET = SERIAL_NUMBER_LENGTH + ICCID_LENGTH;

    /**
     * 每个固件版本字段占 2 字节 BCD。
     */
    public static final int REGISTER_FIRMWARE_LENGTH = 2;

    /**
     * 注册报文内固件版本 1 字段偏移。
     */
    public static final int REGISTER_FIRMWARE1_OFFSET = REGISTER_RSSI_OFFSET + 1;

    /**
     * 注册报文内固件版本 2 字段偏移。
     */
    public static final int REGISTER_FIRMWARE2_OFFSET = REGISTER_FIRMWARE1_OFFSET + REGISTER_FIRMWARE_LENGTH;

    /**
     * 注册报文内固件版本 3 字段偏移。
     */
    public static final int REGISTER_FIRMWARE3_OFFSET = REGISTER_FIRMWARE2_OFFSET + REGISTER_FIRMWARE_LENGTH;

    /**
     * 注册报文内上报周期字段偏移，单位为分钟。
     */
    public static final int REGISTER_REPORT_INTERVAL_OFFSET = REGISTER_FIRMWARE3_OFFSET + REGISTER_FIRMWARE_LENGTH;

    /**
     * 注册报文固定体总长度。
     */
    public static final int REGISTER_BODY_LENGTH = REGISTER_REPORT_INTERVAL_OFFSET + 1;

    /**
     * 对时报文体固定长度。
     */
    public static final int TIME_SYNC_BODY_LENGTH = 7;

    /**
     * 四费率上报时 Modbus 数据区长度。
     */
    public static final int DATA_LENGTH_RATE4 = 0x60;

    /**
     * 八费率上报时 Modbus 数据区长度。
     */
    public static final int DATA_LENGTH_RATE8 = 0x90;

    /**
     * 总电量在 Modbus 数据区中的偏移。
     */
    public static final int RATE_TOTAL_OFFSET = 42;

    /**
     * 尖电量在 Modbus 数据区中的偏移。
     */
    public static final int RATE_HIGHER_OFFSET = 46;

    /**
     * 峰电量在 Modbus 数据区中的偏移。
     */
    public static final int RATE_HIGH_OFFSET = 50;

    /**
     * 平电量在 Modbus 数据区中的偏移。
     */
    public static final int RATE_LOW_OFFSET = 54;

    /**
     * 谷电量在 Modbus 数据区中的偏移。
     */
    public static final int RATE_LOWER_OFFSET = 58;

    /**
     * 深谷电量在八费率 Modbus 数据区中的偏移。
     */
    public static final int RATE_DEEP_LOWER_OFFSET = 96;

    /**
     * 四费率上报时间字段在 Modbus 数据区中的偏移。
     */
    public static final int RATE4_TIME_OFFSET = 80;

    /**
     * 八费率上报时间字段在 Modbus 数据区中的偏移。
     */
    public static final int RATE8_TIME_OFFSET = 80;

    /**
     * 数据段起始标记，对应字符 '['。
     */
    public static final byte SECTION_START = 0x5b;

    /**
     * 数据段结束标记，对应字符 ']'。
     */
    public static final byte SECTION_END = 0x5d;

    /**
     * Modbus 数据流起始标记，对应字符 '('。
     */
    public static final byte MODBUS_START = 0x28;

    /**
     * Modbus 数据流结束标记，对应字符 ')'。
     */
    public static final byte MODBUS_END = 0x29;
}
