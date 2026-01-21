package info.zhihui.ems.foundation.integration.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeviceTypeQueryDto {
    private Integer pid;
    private List<Integer> ids;
    private String typeKey;
}
