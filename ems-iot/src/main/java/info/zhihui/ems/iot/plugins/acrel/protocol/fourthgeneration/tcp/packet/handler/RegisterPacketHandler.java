package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.port.DeviceBinder;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 4G 注册命令处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterPacketHandler implements Acrel4gPacketHandler {

    private final DeviceBinder deviceBinder;
    private final Acrel4gFrameCodec frameCodec;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.REGISTER);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        RegisterMessage msg = (RegisterMessage) message;
        String deviceNo = msg.getSerialNumber();
        if (StringUtils.isBlank(deviceNo)) {
            log.warn("4G 注册缺少序列号，session={}", session.getSessionId());
            reportAbnormal(context, AbnormalReasonEnum.ILLEGAL_DEVICE, "注册缺少序列号");
            return;
        }
        try {
            deviceBinder.bind(context, deviceNo);

            session.send(frameCodec.encodeAck(Acrel4gPacketCode.REGISTER));
            log.info("4G 电表 {} 注册成功，session={}", deviceNo, session.getSessionId());
        } catch (NotFoundException e) {
            log.error("4G 注册设备不存在，deviceNo={} session={}", deviceNo, session.getSessionId());
            reportAbnormal(context, AbnormalReasonEnum.ILLEGAL_DEVICE, "注册设备不存在");
        } catch (Exception e) {
            log.error("4G 注册异常，deviceNo={} session={}", deviceNo, session.getSessionId(), e);
            reportAbnormal(context, AbnormalReasonEnum.BUSINESS_ERROR, "注册处理异常");
        }
    }

    private void reportAbnormal(ProtocolMessageContext context, AbnormalReasonEnum reason, String detail) {
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
