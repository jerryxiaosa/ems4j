package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户订单流水实体类
 */
@Data
@Accessors(chain = true)
@TableName("energy_account_order_flow")
public class OrderFlowEntity {
    private String id;

    /**
     * 消费ID
     */
    private String consumeId;

    /**
     * 账户关联id
     */
    private Integer balanceRelationId;

    /**
     * 余额类型：1账户余额，2电表余额
     */
    private Integer balanceType;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}