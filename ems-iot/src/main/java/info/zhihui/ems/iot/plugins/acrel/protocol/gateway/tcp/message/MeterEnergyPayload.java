package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import java.math.BigDecimal;

/**
 * 网关单表能耗数据。
 *
 * @param meterId 电表标识
 * @param totalEnergy 总电量
 */
public record MeterEnergyPayload(String meterId, BigDecimal totalEnergy) {
}
