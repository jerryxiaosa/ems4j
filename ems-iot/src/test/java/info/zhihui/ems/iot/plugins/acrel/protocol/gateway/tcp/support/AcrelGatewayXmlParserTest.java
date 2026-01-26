package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.MeterEnergyPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class AcrelGatewayXmlParserTest {

    @Test
    void testParseAuth_WhenXmlValid_ShouldReturnMessage() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();
        String xml = """
                <root>
                  <common>
                    <gateway_id>GW-001</gateway_id>
                    <building_id>B1</building_id>
                    <type>id_validate</type>
                  </common>
                  <id_validate operation="sequence" version="2">
                    <sequence>202501010101010001</sequence>
                    <md5>ABC123</md5>
                  </id_validate>
                </root>
                """;

        GatewayAuthMessage message = parser.parseAuth(xml);

        Assertions.assertNotNull(message);
        Assertions.assertEquals("GW-001", message.gatewayId());
        Assertions.assertEquals("B1", message.buildingId());
        Assertions.assertEquals("sequence", message.operation());
        Assertions.assertEquals("202501010101010001", message.sequence());
        Assertions.assertEquals("ABC123", message.md5());
    }

    @Test
    void testParseAuth_WhenMissingValidate_ShouldReturnNull() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();
        String xml = """
                <root>
                  <common>
                    <gateway_id>GW-001</gateway_id>
                  </common>
                </root>
                """;

        Assertions.assertNull(parser.parseAuth(xml));
    }

    @Test
    void testParseAuth_WhenXmlInvalid_ShouldReturnNull() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();

        Assertions.assertNull(parser.parseAuth("<root>"));
    }

    @Test
    void testParseReport_WhenXmlValid_ShouldReturnReport() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();
        String xml = """
                <root>
                  <common>
                    <gateway_id>GW-001</gateway_id>
                  </common>
                  <data>
                    <time>20250101123000</time>
                    <meter id="01">
                      <function id="WPP">123.45</function>
                      <function id="PowerHigher">10.11</function>
                      <function id="PowerHigh">20.22</function>
                      <function id="PowerFlat">30.33</function>
                      <function id="PowerLow">40.44</function>
                      <function id="EPISG">50.55</function>
                    </meter>
                  </data>
                </root>
                """;

        GatewayReportMessage report = parser.parseReport(xml);

        Assertions.assertNotNull(report);
        Assertions.assertEquals("GW-001", report.gatewayId());
        Assertions.assertEquals(LocalDateTime.of(2025, 1, 1, 12, 30), report.reportedAt());
        Assertions.assertNotNull(report.meters());
        Assertions.assertEquals(1, report.meters().size());
        MeterEnergyPayload payload = report.meters().get(0);
        Assertions.assertEquals("01", payload.meterId());
        Assertions.assertEquals(new BigDecimal("123.45"), payload.totalEnergy());
        Assertions.assertEquals(new BigDecimal("10.11"), payload.higherEnergy());
        Assertions.assertEquals(new BigDecimal("20.22"), payload.highEnergy());
        Assertions.assertEquals(new BigDecimal("30.33"), payload.lowEnergy());
        Assertions.assertEquals(new BigDecimal("40.44"), payload.lowerEnergy());
        Assertions.assertEquals(new BigDecimal("50.55"), payload.deepLowEnergy());
    }

    @Test
    void testParseReport_WhenTimeInvalid_ShouldReturnNullTime() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();
        String xml = """
                <root>
                  <common>
                    <gateway_id>GW-001</gateway_id>
                  </common>
                  <data>
                    <time>bad-time</time>
                    <meter id="01">
                      <function id="WPP">1</function>
                    </meter>
                  </data>
                </root>
                """;

        GatewayReportMessage report = parser.parseReport(xml);

        Assertions.assertNotNull(report);
        Assertions.assertNull(report.reportedAt());
    }

    @Test
    void testParseReport_WhenWppMissing_ShouldReturnNullValue() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();
        String xml = """
                <root>
                  <common>
                    <gateway_id>GW-001</gateway_id>
                  </common>
                  <data>
                    <time>20250101123000</time>
                    <meter id="01">
                      <function id="KWH">1</function>
                    </meter>
                  </data>
                </root>
                """;

        GatewayReportMessage report = parser.parseReport(xml);

        List<MeterEnergyPayload> meters = report.meters();
        Assertions.assertEquals(1, meters.size());
        Assertions.assertNull(meters.get(0).totalEnergy());
        Assertions.assertNull(meters.get(0).higherEnergy());
        Assertions.assertNull(meters.get(0).highEnergy());
        Assertions.assertNull(meters.get(0).lowEnergy());
        Assertions.assertNull(meters.get(0).lowerEnergy());
        Assertions.assertNull(meters.get(0).deepLowEnergy());
    }

    @Test
    void testParseReport_WhenXmlBlank_ShouldReturnNull() {
        AcrelGatewayXmlParser parser = new AcrelGatewayXmlParser();

        Assertions.assertNull(parser.parseReport(" "));
    }
}
