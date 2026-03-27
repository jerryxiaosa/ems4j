package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant;

/**
 * 安科瑞 4G 直连帧层常量。
 */
public final class Acrel4gFrameConstants {

    private Acrel4gFrameConstants() {
    }

    /**
     * 4G 直连帧起始分隔符，对应字符 '{'。
     */
    public static final byte DELIMITER = 0x7b;

    /**
     * 4G 直连帧结束分隔符，对应字符 '}'。
     */
    public static final byte DELIMITER_END = 0x7d;
}
