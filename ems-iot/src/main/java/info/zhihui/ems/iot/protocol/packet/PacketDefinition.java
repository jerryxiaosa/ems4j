package info.zhihui.ems.iot.protocol.packet;

import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;

/**
 * Packet definition binds parser and handler for a command.
 *
 * @param <T> message type
 */
public interface PacketDefinition<T extends ProtocolMessage> {

    /**
     * Command key.
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

    /**
     * Handle a parsed protocol message.
     *
     * @param context device message context
     * @param message parsed message
     */
    void handle(ProtocolMessageContext context, T message);

    /**
     * Validate command consistency among definition, parser, and handler.
     *
     * @param parserCommand  parser command
     * @param handlerCommand handler command
     */
    default void validate(String parserCommand, String handlerCommand) {
        String definitionCommand = command();
        if (!definitionCommand.equals(parserCommand) || !definitionCommand.equals(handlerCommand)) {
            throw new IllegalStateException("命令定义与解析/处理不一致，command=" + definitionCommand);
        }
    }
}
