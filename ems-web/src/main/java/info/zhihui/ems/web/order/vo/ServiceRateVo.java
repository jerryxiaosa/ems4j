package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 服务费率配置
 */
@Data
@Accessors(chain = true)
@Schema(name = "ServiceRateVo", description = "默认服务费率配置")
public class ServiceRateVo {

    @NotNull
    @DecimalMin(value = "0", message = "默认服务费比例需在0到1之间")
    @DecimalMax(value = "1", inclusive = false, message = "默认服务费比例需在0到1之间，且不能等于1")
    @Digits(integer = 1, fraction = 4, message = "默认服务费比例最多保留4位小数")
    @Schema(description = "默认服务费率", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal defaultServiceRate;
}
