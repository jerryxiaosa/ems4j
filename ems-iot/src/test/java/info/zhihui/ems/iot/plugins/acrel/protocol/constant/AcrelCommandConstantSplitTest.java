package info.zhihui.ems.iot.plugins.acrel.protocol.constant;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.constant.AcrelGatewayCommandConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AcrelCommandConstantSplitTest {

    @Test
    void shouldExposeAcrel4gCommandConstants() {
        Assertions.assertEquals((byte) 0x84, Acrel4gCommandConstants.REGISTER);
        Assertions.assertEquals((byte) 0x93, Acrel4gCommandConstants.TIME_SYNC);
        Assertions.assertEquals((byte) 0x91, Acrel4gCommandConstants.DATA_UPLOAD);
        Assertions.assertEquals((byte) 0x90, Acrel4gCommandConstants.DOWNLINK);
        Assertions.assertEquals((byte) 0x94, Acrel4gCommandConstants.HEARTBEAT);
    }

    @Test
    void shouldExposeGatewayCommandConstants() {
        Assertions.assertEquals((byte) 0x01, AcrelGatewayCommandConstants.AUTH);
        Assertions.assertEquals((byte) 0x02, AcrelGatewayCommandConstants.HEARTBEAT);
        Assertions.assertEquals((byte) 0x03, AcrelGatewayCommandConstants.DATA);
        Assertions.assertEquals((byte) 0x04, AcrelGatewayCommandConstants.DATA_ZIP);
        Assertions.assertEquals((byte) 0xF1, AcrelGatewayCommandConstants.DOWNLINK);
        Assertions.assertEquals((byte) 0xF2, AcrelGatewayCommandConstants.DOWNLINK_ACK);
    }
}
