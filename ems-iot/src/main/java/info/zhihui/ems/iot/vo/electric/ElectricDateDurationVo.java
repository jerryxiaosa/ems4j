package info.zhihui.ems.iot.vo.electric;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricDateDurationVo {

    @NotBlank(message = "月份不能为空")
    private String month;
    @NotBlank(message = "日期不能为空")
    private String day;
    @NotBlank(message = "日方案不能为空")
    private String dailyPlanId;
}
