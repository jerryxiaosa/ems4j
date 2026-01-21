package info.zhihui.ems.mq.api.message.finance;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.mq.api.message.BaseMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额变动消息
 *
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class BalanceChangedMessage extends BaseMessage {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 余额类型（账户/电表）
     */
    @NotNull(message = "余额类型不能为空")
    private BalanceTypeEnum balanceType;

    /**
     * 余额关联ID（账户ID或电表ID）
     */
    @NotNull(message = "余额关联ID不能为空")
    private Integer balanceRelationId;

    /**
     * 最新余额
     */
    @NotNull(message = "最新余额不能为空")
    private BigDecimal newBalance;

    /**
     * 本次变动金额
     */
    @NotNull(message = "本次变动金额不能为空")
    private BigDecimal changeAmount;

    /**
     * 事件时间
     */
    @NotNull(message = "事件时间不能为空")
    private LocalDateTime eventTime;
}
