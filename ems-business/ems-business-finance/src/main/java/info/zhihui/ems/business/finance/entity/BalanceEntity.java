package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账户余额实体类
 * 对应数据库表：energy_account_balance
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_account_balance")
public class BalanceEntity {
    /**
     * 账户id
     */
    private Integer id;

    /**
     * 账户关联id
     */
    private Integer balanceRelationId;

    /**
     * 余额类型：0账户余额，1电表余额
     */
    private Integer balanceType;

    /**
     * 余额金额
     */
    private BigDecimal balance;

    /**
     * 账户id
     */
    private Integer accountId;

}