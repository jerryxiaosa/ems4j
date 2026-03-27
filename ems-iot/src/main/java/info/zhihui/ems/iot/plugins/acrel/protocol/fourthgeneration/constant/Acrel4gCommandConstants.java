package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant;

/**
 * 安科瑞 4G 直连命令字常量。
 */
public final class Acrel4gCommandConstants {

    private Acrel4gCommandConstants() {
    }

    /**
     * 注册报文命令字。
     */
    public static final byte REGISTER = (byte) 0x84;

    /**
     * 对时报文命令字。
     */
    public static final byte TIME_SYNC = (byte) 0x93;

    /**
     * 数据上报报文命令字。
     */
    public static final byte DATA_UPLOAD = (byte) 0x91;

    /**
     * 下行报文命令字。
     */
    public static final byte DOWNLINK = (byte) 0x90;

    /**
     * 心跳报文命令字。
     */
    public static final byte HEARTBEAT = (byte) 0x94;
}
