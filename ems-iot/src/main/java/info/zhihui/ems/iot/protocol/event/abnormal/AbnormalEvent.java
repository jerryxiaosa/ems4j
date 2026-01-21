package info.zhihui.ems.iot.protocol.event.abnormal;

public record AbnormalEvent(AbnormalReasonEnum reason, long timestampMillis, String detail, boolean forceClose) {

    public AbnormalEvent(AbnormalReasonEnum reason, long timestampMillis, String detail) {
        this(reason, timestampMillis, detail, false);
    }
}
