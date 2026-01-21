package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayTransparentMessage;
import info.zhihui.ems.iot.util.HexUtil;
import org.springframework.stereotype.Component;

/**
 * 网关透明转发编码（meterId|payloadHex）。
 */
@Component
public class AcrelGatewayTransparentCodec {

    public String encode(String meterId, byte[] payload) {
        if (meterId == null) {
            return null;
        }
        return meterId + "|" + HexUtil.bytesToHexString(payload == null ? new byte[0] : payload);
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
