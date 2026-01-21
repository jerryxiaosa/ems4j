package info.zhihui.ems.foundation.integration.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeviceModelQueryDto {

    private List<Integer> typeIds;

    private String typeKey;

    private String manufacturerName;

    private String modelName;

    private String productCode;
}
