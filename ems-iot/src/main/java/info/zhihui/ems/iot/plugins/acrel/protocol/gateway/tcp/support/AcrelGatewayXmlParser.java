package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayAuthMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.GatewayReportMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message.MeterEnergyPayload;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 安科瑞网关 XML 解析器。
 */
@Component
public class AcrelGatewayXmlParser {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int MAX_XML_BYTES = 16 * 1024 * 1024;

    /**
     * 解析网关认证报文。
     * 示例：
     * {@code
     * <root>
     *   <common>
     *     <gateway_id>GW-001</gateway_id>
     *     <building_id>B1</building_id>
     *     <type>id_validate</type>
     *   </common>
     *   <id_validate operation="sequence" version="2">
     *     <sequence>202501010101010001</sequence>
     *   </id_validate>
     * </root>
     * }
     */
    public GatewayAuthMessage parseAuth(String xml) {
        Element root = parseRoot(xml);
        if (root == null) {
            return null;
        }
        Element common = firstElement(root, "common");
        Element validate = firstElement(root, "id_validate");
        if (validate == null) {
            return null;
        }
        String gatewayId = textOf(common, "gateway_id");
        String buildingId = textOf(common, "building_id");
        String operation = validate.getAttribute("operation");
        String sequence = textOf(validate, "sequence");
        String md5 = textOf(validate, "md5");
        return new GatewayAuthMessage(gatewayId, buildingId, operation, sequence, md5);
    }

    /**
     * 解析网关数据上报报文。
     * 示例：
     * {@code
     * <root>
     *   <common>
     *     <gateway_id>GW-001</gateway_id>
     *   </common>
     *   <data>
     *     <time>20250101123000</time>
     *     <meter id="01">
     *     <function id="WPP" error="">433.31</function>
     *     <function id="PowerHigher" error="">34.55</function>
     *     <function id="PowerHigh" error="">75.57</function>
     *     <function id="PowerFlat" error="">192.93</function>
     *     <function id="PowerLow" error="">128.04</function>
     *     <function id="EPISG" error="">2.22</function>
     *     </meter>
     *   </data>
     * </root>
     * }
     *
     * @NOTICE 注意这里的属性id需要在网关上对应配置
     */
    public GatewayReportMessage parseReport(String xml) {
        Element root = parseRoot(xml);
        if (root == null) {
            return null;
        }
        Element common = firstElement(root, "common");
        Element data = firstElement(root, "data");
        String gatewayId = textOf(common, "gateway_id");
        String reportedTime = textOf(data, "time");
        LocalDateTime reportedAt = parseTime(reportedTime);
        List<MeterEnergyPayload> meters = parseMeters(data);
        return new GatewayReportMessage(gatewayId, reportedAt, meters);
    }

    private Element parseRoot(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        Document doc = parse(xml);
        if (doc == null) {
            return null;
        }
        return doc.getDocumentElement();
    }

    private Document parse(String xml) {
        try {
            byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > MAX_XML_BYTES) {
                return null;
            }
            DocumentBuilderFactory factory = createSecureFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(bytes));
        } catch (Exception ex) {
            return null;
        }
    }

    private DocumentBuilderFactory createSecureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private List<MeterEnergyPayload> parseMeters(Element data) {
        List<MeterEnergyPayload> meters = new ArrayList<>();
        if (data == null) {
            return meters;
        }
        NodeList meterNodes = data.getElementsByTagName("meter");
        for (int i = 0; i < meterNodes.getLength(); i++) {
            Element meter = (Element) meterNodes.item(i);
            String meterId = meter.getAttribute("id");
            meters.add(parseMeterEnergyPayload(meterId, meter));
        }
        return meters;
    }

    private MeterEnergyPayload parseMeterEnergyPayload(String meterId, Element meter) {
        BigDecimal totalEnergy = null;
        BigDecimal higherEnergy = null;
        BigDecimal highEnergy = null;
        BigDecimal lowEnergy = null;
        BigDecimal lowerEnergy = null;
        BigDecimal deepLowEnergy = null;
        Integer ct = null;
        NodeList functions = meter.getElementsByTagName("function");
        for (int i = 0; i < functions.getLength(); i++) {
            Element function = (Element) functions.item(i);
            String id = function.getAttribute("id");
            if (StringUtils.isBlank(id)) {
                continue;
            }
            BigDecimal value = parseDecimal(function.getTextContent());
            switch (id.trim().toUpperCase(Locale.ROOT)) {
                case "WPP" -> totalEnergy = value;
                case "POWERHIGHER" -> higherEnergy = value;
                case "POWERHIGH" -> highEnergy = value;
                case "POWERFLAT" -> lowEnergy = value;
                case "POWERLOW" -> lowerEnergy = value;
                case "EPISG" -> deepLowEnergy = value;
                default -> {
                }
            }
        }
        return new MeterEnergyPayload(meterId, totalEnergy, higherEnergy, highEnergy, lowEnergy, lowerEnergy, deepLowEnergy, ct);
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Element firstElement(Element root, String tag) {
        if (root == null) {
            return null;
        }
        NodeList nodes = root.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        return (Element) nodes.item(0);
    }

    private String textOf(Element parent, String tag) {
        Element element = firstElement(parent, tag);
        if (element == null) {
            return null;
        }
        return element.getTextContent();
    }

}
