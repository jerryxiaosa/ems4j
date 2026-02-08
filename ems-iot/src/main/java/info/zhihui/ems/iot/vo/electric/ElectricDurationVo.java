package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricDurationVo {

    @NotNull(message = "时段类型不能为空")
    @Min(value = 0, message = "时段类型范围为0~5")
    @Max(value = 5, message = "时段类型范围为0~5")
    private Integer period;

    @NotBlank(message = "分钟不能为空")
    @Pattern(regexp = "^[0-5]\\d$", message = "分钟格式不正确")
    private String min;

    @NotBlank(message = "小时不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3])$", message = "小时格式不正确")
    private String hour;
}
