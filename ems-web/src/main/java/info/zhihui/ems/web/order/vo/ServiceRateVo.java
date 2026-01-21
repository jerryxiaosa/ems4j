package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "默认服务费率", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal defaultServiceRate;
}
