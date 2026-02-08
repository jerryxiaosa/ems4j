package info.zhihui.ems.iot.vo.electric;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ElectricDurationUpdateVo", description = "日时段电价更新请求参数")
public class ElectricDurationUpdateVo {

    @NotEmpty(message = "日方案时段不能为空")
    @Valid
    @Schema(description = "日时段电价列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ElectricDurationVo> electricDurations;

    @NotNull(message = "日方案编号不能为空")
    @Min(value = 1, message = "日方案编号范围为1~2")
    @Max(value = 2, message = "日方案编号范围为1~2")
    @Schema(description = "日方案编号，范围1~2", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dailyPlanId;
}
