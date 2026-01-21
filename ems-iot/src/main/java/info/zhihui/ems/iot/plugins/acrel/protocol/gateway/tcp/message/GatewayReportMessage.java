package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 网关数据上报解析结果。
 *
 * @param gatewayId 网关编号
 * @param reportedAt 上报时间
 * @param meters 电表数据集合
 */
public record GatewayReportMessage(String gatewayId, LocalDateTime reportedAt,
                                   List<MeterEnergyPayload> meters) implements AcrelMessage {
}
