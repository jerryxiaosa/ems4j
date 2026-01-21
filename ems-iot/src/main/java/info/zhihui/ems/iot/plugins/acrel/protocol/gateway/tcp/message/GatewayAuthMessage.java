package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;

/**
 * 网关认证报文解析结果。
 *
 * @param gatewayId 网关编号
 * @param buildingId 建筑编号
 * @param operation 认证操作类型
 * @param sequence 认证序列号
 * @param md5 认证摘要
 */
public record GatewayAuthMessage(String gatewayId, String buildingId, String operation,
                                 String sequence, String md5) implements AcrelMessage {
}
