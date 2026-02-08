package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricDateDurationVo {

    @NotBlank(message = "月份不能为空")
    @Pattern(regexp = "^(?:[1-9]|1[0-2])$", message = "月份范围为1~12")
    private String month;

    @NotBlank(message = "日期不能为空")
    @Pattern(regexp = "^(?:[1-9]|[12]\\d|3[01])$", message = "日期范围为1~31")
    private String day;

    @NotBlank(message = "日方案不能为空")
    @Pattern(regexp = "^[1-2]$", message = "日方案编号范围为1~2")
    private String dailyPlanId;
}
