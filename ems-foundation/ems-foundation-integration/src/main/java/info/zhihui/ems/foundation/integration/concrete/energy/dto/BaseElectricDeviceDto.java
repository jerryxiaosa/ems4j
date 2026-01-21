package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class BaseElectricDeviceDto {
    private Integer deviceId;

    private Integer areaId;
}
