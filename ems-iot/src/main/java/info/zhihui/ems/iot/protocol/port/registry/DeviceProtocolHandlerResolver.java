package info.zhihui.ems.iot.protocol.port.registry;

/**
 * 协议处理器解析器。
 */
public interface DeviceProtocolHandlerResolver {

    /**
     * 根据协议签名解析处理器。
     *
     * @param signature 协议签名
     * @return 设备协议处理器
     */
    DeviceProtocolHandler resolve(ProtocolSignature signature);
}
