package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 安科瑞网关电表标识转换（meterId <-> portNo + meterAddress）。
 */
@Slf4j
public final class AcrelGatewayMeterIdCodec {

    private static final int METER_ID_LENGTH = 5;
    private static final int PORT_NO_LENGTH = 2;

    private AcrelGatewayMeterIdCodec() {
    }

    public static MeterIdentity parse(String meterId) {
        if (!StringUtils.hasText(meterId)) {
            return null;
        }
        String trimmed = meterId.trim();
        if (trimmed.length() != METER_ID_LENGTH) {
            return null;
        }
        try {
            int portNo = Integer.parseInt(trimmed.substring(0, PORT_NO_LENGTH));
            int meterAddress = Integer.parseInt(trimmed.substring(PORT_NO_LENGTH, METER_ID_LENGTH));
            log.debug("网关电表标识解析成功，portNo={} meterAddress={}", portNo, meterAddress);

            return new MeterIdentity(portNo, meterAddress);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String format(Integer portNo, Integer meterAddress) {
        if (portNo == null || meterAddress == null) {
            throw new IllegalArgumentException("电表标识格式化参数不能为空");
        }
        return String.format("%02d%03d", portNo, meterAddress);
    }

    public record MeterIdentity(int portNo, int meterAddress) {
    }
}
