package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.packet.PacketHandler;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;

/**
 * 4G 命令处理器接口。
 */
public interface Acrel4gPacketHandler extends PacketHandler<AcrelMessage> {
    default void reportAbnormal(ProtocolMessageContext context, AbnormalReasonEnum reason, String detail) {
        if (context == null || reason == null) {
            return;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return;
        }
        session.publishEvent(new AbnormalEvent(reason, System.currentTimeMillis(), detail, true));
    }
}
