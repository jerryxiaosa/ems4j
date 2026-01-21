package info.zhihui.ems.mq.api.message.order.status;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EnergyTopUpSuccessMessage extends BaseOrderStatusMessage {
    /**
     * 订单金额
     */
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderAmount;

    /**
     * 余额类型
     */
    @NotNull(message = "余额类型不能为空")
    private BalanceTypeEnum balanceType;

    /**
     * 账户id
     */
    @NotNull(message = "账户id不能为空")
    private Integer accountId;

    /**
     * 电表id
     */
    private Integer meterId;

    /**
     * 电表类型
     */
    private MeterTypeEnum meterType;
}
