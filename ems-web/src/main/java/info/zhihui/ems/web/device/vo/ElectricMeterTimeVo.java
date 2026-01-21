package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 设置电表时间段请求
 */
@Data
@Schema(name = "ElectricMeterTimeVo", description = "电表时间段设置参数")
public class ElectricMeterTimeVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @NotEmpty
    @Schema(description = "电价时间段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ElectricPriceTimeVo> timeList;
}
