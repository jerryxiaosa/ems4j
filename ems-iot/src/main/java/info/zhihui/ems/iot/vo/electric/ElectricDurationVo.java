package info.zhihui.ems.iot.vo.electric;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ElectricDurationVo", description = "日时段电价项")
public class ElectricDurationVo {

    @NotNull(message = "时段类型不能为空")
    @Min(value = 0, message = "时段类型范围为0~5")
    @Max(value = 5, message = "时段类型范围为0~5")
    @Schema(description = "时段类型编码，范围0~5", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer period;

    @NotBlank(message = "分钟不能为空")
    @Pattern(regexp = "^[0-5]\\d$", message = "分钟格式不正确")
    @Schema(description = "分钟，格式mm，范围00~59", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private String min;

    @NotBlank(message = "小时不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3])$", message = "小时格式不正确")
    @Schema(description = "小时，格式HH，范围00~23", requiredMode = Schema.RequiredMode.REQUIRED, example = "08")
    private String hour;
}
