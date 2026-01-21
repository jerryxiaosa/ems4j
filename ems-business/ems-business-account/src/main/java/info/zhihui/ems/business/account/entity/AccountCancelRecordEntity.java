package info.zhihui.ems.business.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 账号销户记录实体
 *
 * @author jerryxiaosa
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("energy_account_cancel_record")
public class AccountCancelRecordEntity extends BaseEntity {
    private Integer id;

    /**
     * 销户编号
     */
    private String cancelNo;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 账户归属者id
     */
    private Integer ownerId;

    /**
     * 账户类型，0企业1个人
     */
    private Integer ownerType;

    /**
     * 所有人名称
     */
    private String ownerName;

    /**
     * 电费账户类型：0按需、1包月、2合并计费
     */
    private Integer electricAccountType;

    /**
     * 销户电表数量
     */
    private Integer electricMeterAmount;

    /**
     * 是否全部销户
     */
    private Boolean fullCancel;

    /**
     * 0:无处理;1:退款;2:补缴
     */
    private Integer cleanBalanceType;

    /**
     * 结算余额
     */
    private BigDecimal cleanBalanceReal;

    /**
     * 小数点后无法支付的金额
     */
    private BigDecimal cleanBalanceIgnore;

    /**
     * 备注
     */
    private String remark;

}