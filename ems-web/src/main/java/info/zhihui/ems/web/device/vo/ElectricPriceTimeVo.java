package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

/**
 * 电价时间段
 */
@Data
@Schema(name = "ElectricPriceTimeVo", description = "电价时间段")
public class ElectricPriceTimeVo {

    @Schema(description = "电量类型编码，参考 electricDegreeType")
    private Integer type;

    @Schema(description = "开始时间，格式HH:mm:ss")
    private LocalTime start;
}
