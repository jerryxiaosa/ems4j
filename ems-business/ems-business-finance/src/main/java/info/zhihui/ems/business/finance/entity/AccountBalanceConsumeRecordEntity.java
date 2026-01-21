package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能耗包月消费记录
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_account_balance_consume_record")
public class AccountBalanceConsumeRecordEntity {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 消费单号
     */
    private String consumeNo;

    /**
     * 消费类型
     */
    private Integer consumeType;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 所有者ID
     */
    private Integer ownerId;

    /**
     * 所有者类型
     */
    private Integer ownerType;

    /**
     * 所有者名称
     */
    private String ownerName;

    /**
     * 包月支付金额
     */
    private BigDecimal payAmount;

    /**
     * 期初余额
     */
    private BigDecimal beginBalance;

    /**
     * 期末余额
     */
    private BigDecimal endBalance;

    /**
     * 备注
     */
    private String remark;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}