package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * 设备命令翻译器（通用命令 -> 协议命令）。
 *
 * @param <R> 协议请求类型
 */
public interface DeviceCommandTranslator<R> {

    /**
     * 命令类型。
     *
     * @return 设备命令类型
     */
    DeviceCommandTypeEnum type();

    /**
     * 厂商标识。
     *
     * @return 厂商标识
     */
    String vendor();

    /**
     * 产品编码（可选）。
     *
     * <p>返回 {@code null} 表示该翻译器对所有产品通用。</p>
     *
     * @return 产品编码
     */
    default String productCode() {
        return null;
    }

    /**
     * 将领域命令转换为协议命令。
     *
     * @param command 领域命令
     * @return 协议请求
     */
    R toRequest(DeviceCommand command);

    /**
     * 协议请求类型（用于运行期校验）。
     *
     * @return 协议请求类型
     */
    Class<R> requestType();

    /**
     * 解析设备响应。
     *
     * @param command 领域命令
     * @param payload 响应报文
     * @return 命令响应结果
     */
    DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload);
}
