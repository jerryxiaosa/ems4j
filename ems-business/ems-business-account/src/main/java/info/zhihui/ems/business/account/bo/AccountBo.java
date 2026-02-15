package info.zhihui.ems.business.account.bo;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.model.OperatorInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AccountBo extends OperatorInfo {

    /**
     * 账户ID
     */
    private Integer id;

    /**
     * 账户类型
     * @see OwnerTypeEnum
     */
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者id
     */
    private Integer ownerId;

    /**
     * 账户归属者名称
     */
    private String ownerName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系方式
     */
    private String contactPhone;

    /**
     * 电费计费类型
     * @see ElectricAccountTypeEnum
     */
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 包月费用
     */
    private BigDecimal monthlyPayAmount;

    /**
     * 预警方案id
     */
    private Integer warnPlanId;

    /**
     * 电价方案id
     */
    private Integer electricPricePlanId;

    /**
     * 电费预警级别
     */
    private WarnTypeEnum electricWarnType;

}
