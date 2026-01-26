package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import java.math.BigDecimal;

/**
 * 网关单表能耗数据。
 *
 * @param meterId 电表标识
 * @param totalEnergy 总电量
 * @param ct CT变比
 * @param higherEnergy 尖电量
 * @param highEnergy 峰电量
 * @param lowEnergy 平电量
 * @param lowerEnergy 谷电量
 * @param deepLowEnergy 深谷电量
 */
public record MeterEnergyPayload(String meterId,
                                BigDecimal totalEnergy,
                                BigDecimal higherEnergy,
                                BigDecimal highEnergy,
                                BigDecimal lowEnergy,
                                BigDecimal lowerEnergy,
                                BigDecimal deepLowEnergy,
                                Integer ct) {
}
