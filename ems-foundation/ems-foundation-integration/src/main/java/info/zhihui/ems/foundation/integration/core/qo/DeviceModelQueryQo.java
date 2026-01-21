package info.zhihui.ems.foundation.integration.core.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class DeviceModelQueryQo {
    private List<Integer> typeIds;

    private String typeKey;

    private String manufacturerName;

    private String modelName;

    private String productCode;
}
