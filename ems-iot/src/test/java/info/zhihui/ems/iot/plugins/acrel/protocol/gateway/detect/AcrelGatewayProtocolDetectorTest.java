package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.detect;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelGatewayProtocolDetectorTest {

    @Test
    void detectTcp_WhenPayloadNull_ShouldReturnNull() {
        AcrelGatewayProtocolDetector detector = new AcrelGatewayProtocolDetector();

        Assertions.assertNull(detector.detectTcp(null));
    }

    @Test
    void detectTcp_WhenPayloadTooShort_ShouldReturnNull() {
        AcrelGatewayProtocolDetector detector = new AcrelGatewayProtocolDetector();

        Assertions.assertNull(detector.detectTcp(new byte[]{0x1f}));
    }

    @Test
    void detectTcp_WhenHeadMismatch_ShouldReturnNull() {
        AcrelGatewayProtocolDetector detector = new AcrelGatewayProtocolDetector();

        Assertions.assertNull(detector.detectTcp(new byte[]{0x00, 0x00}));
    }

    @Test
    void detectTcp_WhenHeadMatch_ShouldReturnSignature() {
        AcrelGatewayProtocolDetector detector = new AcrelGatewayProtocolDetector();
        byte[] payload = new byte[]{
                (byte) ((AcrelProtocolConstants.GATEWAY_HEAD >> 8) & 0xFF),
                (byte) (AcrelProtocolConstants.GATEWAY_HEAD & 0xFF),
                0x01
        };

        ProtocolSignature signature = detector.detectTcp(payload);

        Assertions.assertNotNull(signature);
        Assertions.assertEquals(AcrelProtocolConstants.VENDOR, signature.getVendor());
        Assertions.assertEquals(DeviceAccessModeEnum.GATEWAY, signature.getAccessMode());
        Assertions.assertEquals(TransportProtocolEnum.TCP, signature.getTransportType());
        Assertions.assertNull(signature.getProductCode());
    }
}
