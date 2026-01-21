package info.zhihui.ems.foundation.integration.core.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeviceTypeQueryQo {
    private Integer pid;
    private List<Integer> ids;
    private String typeKey;
}
