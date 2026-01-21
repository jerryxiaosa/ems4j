package info.zhihui.ems.foundation.integration.core.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class DeviceModelBo {
    private Integer id;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 类型key
     */
    private String typeKey;

    /**
     * 制造商名称
     */
    private String manufacturerName;

    /**
     * 型号名称
     */
    private String modelName;

    /**
     * 产品唯一标识
     */
    private String productCode;

    /**
     * 配置信息
     */
    private Map<String, Object> modelProperty;
}
