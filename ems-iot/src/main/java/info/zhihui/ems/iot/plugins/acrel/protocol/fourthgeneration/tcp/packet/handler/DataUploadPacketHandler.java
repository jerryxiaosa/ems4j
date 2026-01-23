package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.handler;

import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.inbound.ProtocolEnergyReportInboundEvent;
import info.zhihui.ems.iot.protocol.port.session.DeviceBinder;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 4G 数据上报命令处理器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataUploadPacketHandler implements Acrel4gPacketHandler {

    private final ProtocolInboundPublisher protocolInboundPublisher;
    private final DeviceBinder deviceBinder;
    private final Acrel4gFrameCodec frameCodec;

    @Override
    public String command() {
        return Acrel4gPacketCode.commandKey(Acrel4gPacketCode.DATA_UPLOAD);
    }

    @Override
    public void handle(ProtocolMessageContext context, AcrelMessage message) {
        ProtocolSession session = context == null ? null : context.getSession();
        if (session == null) {
            return;
        }
        DataUploadMessage upload = (DataUploadMessage) message;
        String deviceNo = upload.getSerialNumber();

        if (StringUtils.isBlank(context.getDeviceNo()) && StringUtils.isBlank(deviceNo)) {
            log.warn("4G 注册缺少序列号，session={}", session.getSessionId());
            reportAbnormal(context, AbnormalReasonEnum.ILLEGAL_DEVICE, "数据上报缺少序列号");
            return;
        }

        if (StringUtils.isNotBlank(deviceNo) && !deviceNo.equals(context.getDeviceNo())) {
            try {
                deviceBinder.bind(context, deviceNo);
            } catch (Exception ex) {
                log.warn("4G 对时绑定设备失败 deviceNo={} session={}", deviceNo, session.getSessionId(), ex);
                reportAbnormal(context, AbnormalReasonEnum.BUSINESS_ERROR, " 数据上报绑定设备失败");
                return;
            }
        } else {
            deviceNo = context.getDeviceNo();
        }

        LocalDateTime receivedAt = context.getReceivedAt() != null ? context.getReceivedAt() : LocalDateTime.now();
        LocalDateTime reportedAt = upload.getTime() != null ? upload.getTime() : receivedAt;
        ProtocolEnergyReportInboundEvent event = new ProtocolEnergyReportInboundEvent()
                .setDeviceNo(deviceNo)
                .setMeterAddress(upload.getMeterAddress())
                .setTotalEnergy(upload.getTotalEnergy())
                .setHigherEnergy(upload.getHigherEnergy())
                .setHighEnergy(upload.getHighEnergy())
                .setLowEnergy(upload.getLowEnergy())
                .setLowerEnergy(upload.getLowerEnergy())
                .setDeepLowEnergy(upload.getDeepLowEnergy())
                .setReportedAt(reportedAt)
                .setReceivedAt(receivedAt)
                .setTransportType(context.getTransportType())
                .setSessionId(session.getSessionId())
                .setRawPayload(HexUtil.bytesToHexString(context.getRawPayload()));
        try {
            protocolInboundPublisher.publish(event);
        } catch (Exception ex) {
            log.warn("4G 上报事件发布异常 deviceNo={} session={}", deviceNo, session.getSessionId(), ex);
            return;
        }
        session.send(frameCodec.encodeAck(Acrel4gPacketCode.DATA_UPLOAD));
    }
}
