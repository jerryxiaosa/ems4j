package info.zhihui.ems.business.account.dto;


import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.business.device.dto.MeterOpenDetailDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class OpenAccountDto {

    /**
     * 账户所有人Id
     */
    @NotNull(message = "业主id不能为空")
    private Integer ownerId;

    /**
     * 账户类型
     * @see OwnerTypeEnum
     */
    @NotNull(message = "业主类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 账户所有人名称
     */
    @NotBlank(message = "业主名称不能为空")
    private String ownerName;

    /**
     * 电费账户类型
     */
    @NotNull(message = "电费账户类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 月度缴费金额
     */
    @DecimalMin(value = "1", message = "月租费必须大于1")
    private BigDecimal monthlyPayAmount;

    /**
     * 电费计费方案
     */
    private Integer electricPricePlanId;

    /**
     * 预警计划id
     */
    private Integer warnPlanId;

    /**
     * 开户表
     */
    @NotEmpty(message = "开户表不能为空")
    private List<MeterOpenDetailDto> electricMeterList;

    /**
     * 是否继承历史阶梯量（默认 false）
     */
    private Boolean inheritHistoryPower;

}
