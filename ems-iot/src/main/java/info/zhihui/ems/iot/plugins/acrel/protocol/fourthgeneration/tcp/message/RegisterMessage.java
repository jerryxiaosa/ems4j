package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RegisterMessage implements AcrelMessage {

    /**
     * 注册序列号（最长 20 字节，字符串以 {@code '\\0'} 结束，不足补零）。
     */
    private String serialNumber;

    /**
     * 卡号/ICCID（最长 30 字节，字符串以 {@code '\\0'} 结束，不足补零）。
     */
    private String iccid;

    /**
     * 信号强度 RSSI（1~31）。
     */
    private int rssi;

    /**
     * 固件版本 1（BCD）。
     */
    private String firmware1;

    /**
     * 固件版本 2（BCD）。
     */
    private String firmware2;

    /**
     * 固件版本 3（BCD）。
     */
    private String firmware3;

    /**
     * 定时上传间隔（分钟，默认 5）。
     */
    private int reportIntervalMinutes;
}
