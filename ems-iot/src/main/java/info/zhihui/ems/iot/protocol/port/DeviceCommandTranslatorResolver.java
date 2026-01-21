package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * Port for resolving device command translators.
 */
public interface DeviceCommandTranslatorResolver {

    /**
     * Resolve a translator by vendor, product, and command type.
     *
     * @param vendor      vendor code
     * @param productCode product code (nullable)
     * @param type        command type
     * @param requestType expected request type
     * @param <R>         request type
     * @return translator
     */
    <R> DeviceCommandTranslator<R> resolve(String vendor, String productCode,
                                           DeviceCommandTypeEnum type, Class<R> requestType);
}
