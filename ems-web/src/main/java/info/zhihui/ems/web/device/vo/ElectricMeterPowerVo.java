package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电量查询结果
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterPowerVo", description = "电表电量结果")
public class ElectricMeterPowerVo {

    @Schema(description = "电量类型编码，参考 electricDegreeType")
    private Integer type;

    @Schema(description = "电量值（kWh）")
    private BigDecimal value;
}
