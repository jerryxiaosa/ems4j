package info.zhihui.ems.iot.protocol.packet;

import info.zhihui.ems.iot.protocol.port.ProtocolMessageContext;

/**
 * Protocol packet handler interface.
 *
 * @param <T> message type
 */
public interface PacketHandler<T extends ProtocolMessage> {

    /**
     * Command key supported by this handler.
     *
     * @return command key
     */
    String command();

    /**
     * Handle a parsed protocol message.
     *
     * @param context device message context
     * @param message parsed message
     */
    void handle(ProtocolMessageContext context, T message);
}
