package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 尖峰平谷电价配置
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricPriceTypeVo", description = "尖峰平谷电价配置")
public class ElectricPriceTypeVo {

    @Schema(description = "电价类型编码，参考 electricDegreeType")
    private Integer type;

    @Schema(description = "电价")
    private BigDecimal price;
}
