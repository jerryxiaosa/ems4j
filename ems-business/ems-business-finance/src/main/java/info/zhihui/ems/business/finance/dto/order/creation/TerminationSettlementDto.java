package info.zhihui.ems.business.finance.dto.order.creation;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销户/销表结算详情 DTO。
 */
@Data
@Accessors(chain = true)
public class TerminationSettlementDto {

    /**
     * 销户编号
     */
    @NotEmpty(message = "销户编号不能为空")
    private String cancelNo;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 账户归属者id
     */
    @NotNull(message = "账户归属者id不能为空")
    private Integer ownerId;

    /**
     * 账户类型
     */
    @NotNull(message = "账户类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者名称
     */
    @NotNull(message = "账户归属者名称不能为空")
    private String ownerName;

    /**
     * 结算金额
     */
    @NotNull(message = "结算金额不能为空")
    private BigDecimal settlementAmount;

    /**
     * 电费计费类型
     */
    @NotNull(message = "电费计费类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 销表数量
     */
    @NotNull(message = "电费数量不能为空")
    private Integer electricMeterAmount;

    /**
     * 是否全部销户
     */
    @NotNull(message = "是否全取消不能为空")
    private Boolean fullCancel;

    /**
     * 电表id列表
     */
    @NotEmpty(message = "电表id列表不能为空")
    private List<Integer> meterIdList;

    /**
     * 终止原因
     */
    private String closeReason;

}
