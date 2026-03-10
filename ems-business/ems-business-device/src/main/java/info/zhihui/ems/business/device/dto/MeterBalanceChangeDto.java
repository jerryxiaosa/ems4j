package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电表余额变化处理参数
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MeterBalanceChangeDto {

    /**
     * 电表ID
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    /**
     * 最新余额
     */
    @NotNull(message = "最新余额不能为空")
    private BigDecimal newBalance;

    /**
     * 是否需要处理自动开关闸
     */
    @NotNull(message = "是否需要处理自动开关闸不能为空")
    private Boolean needHandleSwitchStatus;
}
