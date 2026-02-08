package info.zhihui.ems.iot.vo.electric;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "ElectricDateDurationVo", description = "指定日期电价方案项")
public class ElectricDateDurationVo {

    @NotBlank(message = "月份不能为空")
    @Pattern(regexp = "^(?:[1-9]|1[0-2])$", message = "月份范围为1~12")
    @Schema(description = "月份，范围1~12", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String month;

    @NotBlank(message = "日期不能为空")
    @Pattern(regexp = "^(?:[1-9]|[12]\\d|3[01])$", message = "日期范围为1~31")
    @Schema(description = "日期，范围1~31", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private String day;

    @NotBlank(message = "日方案不能为空")
    @Pattern(regexp = "^[1-2]$", message = "日方案编号范围为1~2")
    @Schema(description = "日方案编号，范围1~2", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String dailyPlanId;
}
