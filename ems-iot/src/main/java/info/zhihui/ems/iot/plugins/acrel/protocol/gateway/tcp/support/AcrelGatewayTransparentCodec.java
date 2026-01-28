package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.util.HexUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 网关透明转发编码（meterId|payloadHex）。
 */
@Component
public class AcrelGatewayTransparentCodec {

    public byte[] encode(Integer portNo, Integer meterAddress, byte[] payload) {
        if (portNo == null || meterAddress == null || payload == null) {
            throw new IllegalArgumentException("网关透明转发编码参数不能为空");
        }
        String meterId = AcrelGatewayMeterIdCodec.format(portNo, meterAddress);
        return encodeMeterId(meterId, payload);
    }

    private byte[] encodeMeterId(String meterId, byte[] payload) {
        byte[] meterBytes = meterId.getBytes(StandardCharsets.UTF_8);
        byte[] hexBytes = HexUtil.bytesToHexBytesLower(payload);
        byte[] result = new byte[meterBytes.length + 1 + hexBytes.length];
        // payload前拼上meterId
        System.arraycopy(meterBytes, 0, result, 0, meterBytes.length);
        result[meterBytes.length] = (byte) '|';
        if (hexBytes.length > 0) {
            System.arraycopy(hexBytes, 0, result, meterBytes.length + 1, hexBytes.length);
        }
        return result;
    }

    public GatewayTransparentMessage decode(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int index = text.indexOf('|');
        if (index <= 0 || index >= text.length() - 1) {
            return null;
        }
        String meterId = text.substring(0, index);
        String payloadHex = text.substring(index + 1);
        byte[] payload = HexUtil.hexStringToByteArray(payloadHex);
        return new GatewayTransparentMessage(meterId, payload);
    }
}
