package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricDurationUpdateVo {

    @NotEmpty(message = "日方案时段不能为空")
    private List<ElectricDurationVo> electricDurations;

    @NotNull(message = "日方案编号不能为空")
    private Integer dailyPlanId;
}
