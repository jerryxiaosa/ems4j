package info.zhihui.ems.iot.protocol.port;

/**
 * 协议会话常用属性键集合。
 */
public final class CommonProtocolSessionKeys {

    public static final ProtocolSessionKey<String> DEVICE_NO =
            new DefaultProtocolSessionKey<>("deviceNo", String.class);

    public static final ProtocolSessionKey<String> GATEWAY_AUTH_SEQUENCE =
            new DefaultProtocolSessionKey<>("gatewayAuthSequence", String.class);

    private CommonProtocolSessionKeys() {
    }

    private static final class DefaultProtocolSessionKey<T> implements ProtocolSessionKey<T> {
        private final String name;
        private final Class<T> type;

        private DefaultProtocolSessionKey(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<T> type() {
            return type;
        }
    }
}
