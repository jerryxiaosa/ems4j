package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricDurationUpdateVo {

    @NotEmpty(message = "计划周期不能为空")
    private List<ElectricDurationVo> electricDurations;

    @NotNull(message = "计划不能为空")
    private Integer plan;
}
