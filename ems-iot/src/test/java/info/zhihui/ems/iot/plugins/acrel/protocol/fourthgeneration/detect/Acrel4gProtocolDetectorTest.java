package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.detect;

import info.zhihui.ems.iot.protocol.port.registry.ProtocolSignature;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.constants.AcrelProtocolConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Acrel4gProtocolDetectorTest {

    @Test
    void detectTcp_WhenPayloadNull_ShouldReturnNull() {
        Acrel4gProtocolDetector detector = new Acrel4gProtocolDetector();

        Assertions.assertNull(detector.detectTcp(null));
    }

    @Test
    void detectTcp_WhenPayloadTooShort_ShouldReturnNull() {
        Acrel4gProtocolDetector detector = new Acrel4gProtocolDetector();

        Assertions.assertNull(detector.detectTcp(new byte[]{AcrelProtocolConstants.DELIMITER}));
    }

    @Test
    void detectTcp_WhenDelimiterMismatch_ShouldReturnNull() {
        Acrel4gProtocolDetector detector = new Acrel4gProtocolDetector();

        Assertions.assertNull(detector.detectTcp(new byte[]{0x00, 0x00}));
    }

    @Test
    void detectTcp_WhenDelimiterMatch_ShouldReturnSignature() {
        Acrel4gProtocolDetector detector = new Acrel4gProtocolDetector();

        ProtocolSignature signature = detector.detectTcp(new byte[]{
                AcrelProtocolConstants.DELIMITER, AcrelProtocolConstants.DELIMITER, 0x01
        });

        Assertions.assertNotNull(signature);
        Assertions.assertEquals(AcrelProtocolConstants.VENDOR, signature.getVendor());
        Assertions.assertEquals(DeviceAccessModeEnum.DIRECT, signature.getAccessMode());
        Assertions.assertEquals(TransportProtocolEnum.TCP, signature.getTransportType());
        Assertions.assertNull(signature.getProductCode());
    }
}
