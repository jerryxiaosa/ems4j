package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * 尖峰平谷时间段配置
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricPriceTimeSettingVo", description = "尖峰平谷时间段配置")
public class ElectricPriceTimeSettingVo {

    @NotNull(message = "电量类型不能为空")
    @Min(value = 1, message = "电量类型编码范围应为1-5")
    @Max(value = 5, message = "电量类型编码范围应为1-5")
    @Schema(description = "电量类型编码，参考 electricDegreeType")
    private Integer type;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间，格式HH:mm:ss")
    private LocalTime start;
}
