package info.zhihui.ems.business.device.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author jerryxiaosa
 */
@Slf4j
public class DeviceUtil {

    /**
     * 判断是否为NB设备
     * @param modelName 设备型号名称
     * @return boolean
     */
    public static boolean isNbCommunicateModel(String modelName) {
        if (StringUtils.isBlank(modelName)) {
            return false;
        }
        return modelName.toLowerCase().contains("nb");
    }

    /**
     * 获取设备属性
     * @param properties 属性
     * @param key 属性key
     * @param type 属性类型
     * @return 属性值
     * @param <T> 属性类型
     */
    public static <T> T getProperty(Map<String, Object> properties, String key, Class<T> type) {
        if (properties == null) {
            return null;
        }
        Object o = properties.get(key);
        if (o == null) {
            return null;
        }
        return type.cast(o);
    }

}
