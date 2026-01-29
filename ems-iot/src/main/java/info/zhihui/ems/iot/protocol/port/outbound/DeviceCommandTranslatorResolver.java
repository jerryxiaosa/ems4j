package info.zhihui.ems.iot.protocol.port.outbound;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * 命令转换器解析端口。
 */
public interface DeviceCommandTranslatorResolver {

    /**
     * 按厂商、型号与命令类型解析转换器。
     *
     * @param vendor      厂商编码
     * @param productCode 产品型号编码（可为空）
     * @param type        命令类型
     * @param requestType 期望请求类型
     * @param <R>         请求类型
     * @return 命令转换器
     */
    <R> DeviceCommandTranslator<R> resolve(String vendor, String productCode,
                                           DeviceCommandTypeEnum type, Class<R> requestType);
}
