package info.zhihui.ems.business.finance.dto.order.creation;

import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class EnergyTopUpDto {
    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 充值账户余额类型
     */
    @NotNull(message = "充值账户类型不能为空")
    private BalanceTypeEnum balanceType;

    /**
     * 账户类型
     *
     * @see OwnerTypeEnum
     */
    @NotNull(message = "账户类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者id
     */
    @NotNull(message = "账户归属者id不能为空")
    private Integer ownerId;

    /**
     * 账户归属者名称
     */
    @NotEmpty(message = "账户归属者名称不能为空")
    private String ownerName;

    /**
     * 电费计费类型
     *
     * @see ElectricAccountTypeEnum
     */
    @NotNull(message = "电费计费类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 表ID
     */
    private Integer meterId;

    /**
     * 表类型
     */
    private MeterTypeEnum meterType;

    /**
     * 表名称
     */
    private String meterName;

    /**
     * 表编号
     */
    private String deviceNo;

    /**
     * 空间id
     */
    private Integer spaceId;

}
