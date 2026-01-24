package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.message;

import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;

/**
 * 网关透传载荷模型。
 *
 * @param meterId 电表标识
 * @param payload 透传的原始数据
 */
public record GatewayTransparentMessage(String meterId, byte[] payload) implements AcrelMessage {
}
