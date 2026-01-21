package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.port.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.TimeSyncMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 4G 对时命令处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeSyncPacketHandler implements Acrel4gPacketHandler {

    private final DeviceBinder deviceBinder;
    private final Acrel4gFrameCodec frameCodec;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.TIME_SYNC);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        TimeSyncMessage msg = (TimeSyncMessage) message;
        String deviceNo = msg.getSerialNumber();
        if (StringUtils.isBlank(deviceNo)) {
            log.debug("4G 对时报文缺少序列号，session={}", session.getSessionId());
        } else {
            try {
                deviceBinder.bind(context, deviceNo);
            } catch (Exception ex) {
                log.warn("4G 对时绑定设备失败 deviceNo={} session={}", deviceNo, session.getSessionId(), ex);
            }
        }

        LocalDateTime receivedAt = context.getReceivedAt() == null ? LocalDateTime.now() : context.getReceivedAt();
        byte[] frame = frameCodec.encode(Acrel4gPacketCode.TIME_SYNC, buildTimeSyncBody(receivedAt));
        session.send(frame);
        log.debug("4G 对时响应已下发 session={}", session.getSessionId());
    }

    private byte[] buildTimeSyncBody(LocalDateTime time) {
        // 年份使用两位（2024 -> 0x18），周日0，周一1...
        byte[] body = new byte[7];
        int year = time.getYear() % 100;
        body[0] = (byte) year;
        body[1] = (byte) time.getMonthValue();
        body[2] = (byte) time.getDayOfMonth();
        int dayOfWeek = time.getDayOfWeek().getValue() % 7; // 周日=0
        body[3] = (byte) dayOfWeek;
        body[4] = (byte) time.getHour();
        body[5] = (byte) time.getMinute();
        body[6] = (byte) time.getSecond();
        return body;
    }
}
