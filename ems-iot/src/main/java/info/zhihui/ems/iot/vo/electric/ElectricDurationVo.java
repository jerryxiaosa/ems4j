package info.zhihui.ems.iot.vo.electric;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricDurationVo {

    private Integer period;
    private String min;
    private String hour;
}
