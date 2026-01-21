package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电表销户余额
 */
@Data
@Accessors(chain = true)
@Schema(name = "MeterCancelBalanceVo", description = "电表销户余额信息")
public class MeterCancelBalanceVo {

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "结算余额")
    private BigDecimal balance;

    @Schema(description = "销户时本年度阶梯累计用量")
    private BigDecimal historyPowerTotal;
}
