package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "电量类型编码，参考 electricDegreeType")
    private Integer type;

    @Schema(description = "开始时间，格式HH:mm:ss")
    private LocalTime start;
}
