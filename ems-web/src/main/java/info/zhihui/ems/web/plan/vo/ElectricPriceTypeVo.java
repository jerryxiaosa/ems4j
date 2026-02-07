package info.zhihui.ems.web.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "电价类型不能为空")
    @Min(value = 1, message = "电价类型编码范围应为1-5")
    @Max(value = 5, message = "电价类型编码范围应为1-5")
    @Schema(description = "电价类型编码，参考 electricDegreeType")
    private Integer type;

    @NotNull(message = "电价不能为空")
    @DecimalMin(value = "0", message = "电价不能小于0")
    @Schema(description = "电价")
    private BigDecimal price;
}
