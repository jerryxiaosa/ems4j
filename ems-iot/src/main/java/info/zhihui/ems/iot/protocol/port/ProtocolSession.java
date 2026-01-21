package info.zhihui.ems.iot.protocol.port;

/**
 * 协议会话抽象，屏蔽具体传输实现。
 */
public interface ProtocolSession {

    /**
     * 会话唯一标识，便于定位。
     */
    String getSessionId();

    /**
     * 会话是否可用。
     */
    boolean isActive();

    /**
     * 发送原始数据。
     *
     * @param payload 原始负载
     */
    void send(byte[] payload);

    /**
     * 触发会话内事件。
     *
     * @param event 事件对象
     */
    void publishEvent(Object event);

    /**
     * 获取会话属性。
     *
     * @param key 属性键
     * @param <T> 属性类型
     * @return 属性值
     */
    <T> T getAttribute(ProtocolSessionKey<T> key);

    /**
     * 设置会话属性。
     *
     * @param key 属性键
     * @param value 属性值
     * @param <T> 属性类型
     */
    <T> void setAttribute(ProtocolSessionKey<T> key, T value);

    /**
     * 移除会话属性。
     *
     * @param key 属性键
     * @param <T> 属性类型
     */
    <T> void removeAttribute(ProtocolSessionKey<T> key);

    /**
     * 关闭会话。
     */
    void close();
}
