package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;

/**
 * 网关数据上报消息。
 *
 * @param report 解析后的上报数据
 * @param rawXml 原始 XML 字符串
 */
public record GatewayDataMessage(GatewayReportMessage report, String rawXml) implements AcrelMessage {
}
