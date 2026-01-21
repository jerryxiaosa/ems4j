package info.zhihui.ems.iot.protocol.packet;

import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;

/**
 * Protocol packet parser interface.
 *
 * @param <T> message type
 */
public interface PacketParser<T extends ProtocolMessage> {

    /**
     * Command key supported by this parser.
     *
     * @return command key
     */
    String command();

    /**
     * Parse payload into a protocol message.
     *
     * @param context device message context
     * @param payload payload bytes
     * @return parsed message, or null if parsing fails
     */
    T parse(ProtocolMessageContext context, byte[] payload);
}
