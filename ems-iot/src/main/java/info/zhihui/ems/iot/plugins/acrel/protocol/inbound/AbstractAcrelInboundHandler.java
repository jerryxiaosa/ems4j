package info.zhihui.ems.iot.plugins.acrel.protocol.inbound;

import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalReasonEnum;
import info.zhihui.ems.iot.protocol.event.abnormal.AbnormalEvent;
import info.zhihui.ems.iot.protocol.decode.FrameDecodeResult;
import info.zhihui.ems.iot.protocol.decode.ProtocolDecodeErrorEnum;
import info.zhihui.ems.iot.protocol.packet.PacketDefinition;
import info.zhihui.ems.iot.protocol.packet.ProtocolMessage;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import info.zhihui.ems.iot.protocol.port.session.ProtocolSession;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 安科瑞协议上行处理模板。
 */
@Slf4j
public abstract class AbstractAcrelInboundHandler {

    /**
     * 上行入口模板，包含解帧、路由、解析与异常上报。
     *
     * @param context 上行消息上下文
     */
    public final void handle(ProtocolMessageContext context) {
        if (context == null) {
            return;
        }
        FrameDecodeResult frame = decode(context.getRawPayload());
        if (frame.reason() != null) {
            logDecodeError(sessionId(context), frame.reason(), context.getRawPayload());
            reportAbnormal(context, mapDecodeReason(frame.reason()), protocolName() + "解帧失败");
            return;
        }
        String commandKey = frame.commandKey();
        log.debug("{} 收到命令 {}={} session={}", handlerName(), commandLabel(), commandKey, sessionId(context));

        PacketDefinition<? extends ProtocolMessage> definition = resolveDefinition(commandKey);
        if (definition == null) {
            logUnknownCommand(sessionId(context), commandKey);
            reportAbnormal(context, AbnormalReasonEnum.UNKNOWN_COMMAND,
                    protocolName() + "命令未定义 " + commandLabel() + "=" + commandKey);
            return;
        }
        parseAndHandle(definition, context, frame.payload(), commandKey);
    }

    /**
     * 解码原始报文为协议帧。
     *
     * @param raw 原始报文
     * @return 解码结果
     */
    protected abstract FrameDecodeResult decode(byte[] raw);

    /**
     * 解析命令定义，用于后续解析与处理。
     *
     * @param commandKey 命令键
     * @return 命令定义
     */
    protected abstract PacketDefinition<? extends ProtocolMessage> resolveDefinition(String commandKey);

    /**
     * 协议名称，用于异常详情拼接。
     *
     * @return 协议名称
     */
    protected abstract String protocolName();

    /**
     * 命令标识字段名，用于日志与异常详情拼接。
     *
     * @return 命令字段名
     */
    protected String commandLabel() {
        return "command";
    }

    /**
     * 记录解帧失败日志。
     */
    protected void logDecodeError(String sessionId, ProtocolDecodeErrorEnum reason, byte[] raw) {
        log.warn("{} 报文解帧失败，session={} reason={} raw={}", handlerName(), sessionId, reason, HexUtil.bytesToHexString(raw));
    }

    /**
     * 记录未找到命令定义的日志。
     */
    protected void logUnknownCommand(String sessionId, String commandKey) {
        log.debug("{} 未找到命令定义 {}={} session={}", handlerName(), commandLabel(), commandKey, sessionId);
    }

    /**
     * 记录命令解析失败日志。
     */
    protected void logParseFailure(String sessionId, String commandKey) {
        log.warn("{} 命令解析失败 {}={} session={}", handlerName(), commandLabel(), commandKey, sessionId);
    }

    /**
     * 上报协议异常事件。
     */
    protected void reportAbnormal(ProtocolMessageContext context, AbnormalReasonEnum reason, String detail) {
        if (context == null || reason == null) {
            return;
        }
        ProtocolSession session = context.getSession();
        if (session == null) {
            return;
        }
        session.publishEvent(new AbnormalEvent(reason, System.currentTimeMillis(), detail));
    }

    /**
     * 提取会话标识，便于日志记录。
     */
    protected String sessionId(ProtocolMessageContext context) {
        if (context == null) {
            return null;
        }
        ProtocolSession session = context.getSession();
        return session == null ? null : session.getSessionId();
    }

    /**
     * 统一映射解码错误到异常原因。
     */
    protected AbnormalReasonEnum mapDecodeReason(ProtocolDecodeErrorEnum reason) {
        if (reason == null) {
            return null;
        }
        return switch (reason) {
            case CRC_INVALID -> AbnormalReasonEnum.CRC_INVALID;
            case FRAME_TOO_SHORT -> AbnormalReasonEnum.FRAME_TOO_SHORT;
            case FRAME_DELIMITER_MISSING -> AbnormalReasonEnum.FRAME_DELIMITER_MISSING;
        };
    }

    /**
     * 获取当前处理器名称，用于统一日志前缀。
     */
    protected String handlerName() {
        return getClass().getSimpleName();
    }

    /**
     * 执行解析与处理，并处理解析失败的异常路径。
     */
    private <T extends ProtocolMessage> void parseAndHandle(PacketDefinition<T> definition,
                                                            ProtocolMessageContext context,
                                                            byte[] payload,
                                                            String commandKey) {
        T message = definition.parse(context, payload);
        if (message == null) {
            logParseFailure(sessionId(context), commandKey);
            reportAbnormal(context, AbnormalReasonEnum.PAYLOAD_PARSE_ERROR,
                    protocolName() + "命令解析失败 " + commandLabel() + "=" + commandKey);
            return;
        }
        definition.handle(context, message);
    }
}
