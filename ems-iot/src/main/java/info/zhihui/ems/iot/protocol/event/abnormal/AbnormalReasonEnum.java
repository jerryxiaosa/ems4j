package info.zhihui.ems.iot.protocol.event.abnormal;

public enum AbnormalReasonEnum {
    CRC_INVALID,
    FRAME_TOO_SHORT,
    FRAME_DELIMITER_MISSING,
    UNKNOWN_COMMAND,
    PAYLOAD_PARSE_ERROR,
    UNSUPPORTED_PROTOCOL,
    ILLEGAL_DEVICE,
    BUSINESS_ERROR
}
