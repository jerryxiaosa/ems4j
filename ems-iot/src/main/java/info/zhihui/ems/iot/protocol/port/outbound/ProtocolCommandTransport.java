package info.zhihui.ems.iot.protocol.port.outbound;

import java.util.concurrent.CompletableFuture;

/**
 * 协议下行与回执处理的传输端口。
 */
public interface ProtocolCommandTransport {

    /**
     * 发送并等待设备响应。
     *
     * @param deviceNo 设备编号
     * @param payload  下行报文
     * @return 响应数据
     */
    CompletableFuture<byte[]> sendWithAck(String deviceNo, byte[] payload);

    /**
     * 完成等待中的响应。
     *
     * @param deviceNo 设备编号
     * @param payload 响应报文
     * @return 是否完成
     * @throws IllegalArgumentException 设备编号为空
     * @throws IllegalStateException 未找到会话或无挂起任务
     */
    boolean completePending(String deviceNo, byte[] payload);

}
