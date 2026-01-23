package info.zhihui.ems.iot.protocol.port.registry;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;

import java.util.concurrent.CompletableFuture;
import java.util.Set;

/**
 * 设备协议处理器（插件入口）。
 * 负责上行解析与命令下发。
 */
public interface DeviceProtocolHandler {

    /**
     * 厂商标识（用于路由，与 ProtocolSignature.vendor 对齐）。
     */
    String getVendor();

    /**
     * 接入方式（直连/网关），可为空表示不区分。
     */
    DeviceAccessModeEnum getAccessMode();

    /**
     * 处理设备上行报文（尽量避免耗时/阻塞操作）。
     *
     * @param context 上行报文上下文
     */
    void onMessage(ProtocolMessageContext context);

    /**
     * 下发命令并返回响应结果。
     *
     * @param command 领域命令对象
     */
    CompletableFuture<DeviceCommandResult> sendCommand(DeviceCommand command);

    /**
     * 支持的传输协议，默认仅支持 TCP。
     */
    default Set<TransportProtocolEnum> getSupportedTransports() {
        return Set.of(TransportProtocolEnum.TCP);
    }

    /**
     * 产品编码（可选，为空表示不区分产品）。
     */
    default String getProductCode() {
        return null;
    }

}
