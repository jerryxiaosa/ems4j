package info.zhihui.ems.iot.protocol.port.session;

/**
 * 协议会话属性键。
 *
 * @param <T> 属性类型
 */
public interface ProtocolSessionKey<T> {

    /**
     * 属性名（用于底层映射）。
     */
    String name();

    /**
     * 属性类型（用于类型提示）。
     */
    Class<T> type();

}
