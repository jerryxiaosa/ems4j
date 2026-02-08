package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricDurationUpdateVo {

    @NotEmpty(message = "日方案时段不能为空")
    @Valid
    private List<ElectricDurationVo> electricDurations;

    @NotNull(message = "日方案编号不能为空")
    @Min(value = 1, message = "日方案编号范围为1~2")
    @Max(value = 2, message = "日方案编号范围为1~2")
    private Integer dailyPlanId;
}
