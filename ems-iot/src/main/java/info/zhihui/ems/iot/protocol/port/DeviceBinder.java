package info.zhihui.ems.iot.protocol.port;

/**
 * Port for binding a device to a protocol session.
 */
public interface DeviceBinder {

    /**
     * Bind device and session based on the inbound message context.
     *
     * @param context  protocol message context
     * @param deviceNo device number
     */
    void bind(ProtocolMessageContext context, String deviceNo);
}
