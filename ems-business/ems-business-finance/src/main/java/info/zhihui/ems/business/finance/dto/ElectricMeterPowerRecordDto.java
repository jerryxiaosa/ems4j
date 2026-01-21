package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterPowerRecordDto {

    /**
     * 原始数据上报ID
     */
    @NotNull(message = "原始数据上报ID不能为空")
    private String originalReportId;

    /**
     * 电表及电量数据
     */
    @Valid
    @NotNull(message = "电表及电量数据不能为空")
    private ElectricMeterDetailDto electricMeterDetailDto;

    /**
     * 账户Id
     */
    private Integer accountId;

    /**
     * 账户归属者id
     */
    private Integer ownerId;

    /**
     * 账户归属者类型
     */
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者名称
     */
    private String ownerName;

    /**
     * 电费计费类型
     *
     * @see ElectricAccountTypeEnum
     */
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 电表读数
     */
    private BigDecimal power;
    private BigDecimal powerHigher;
    private BigDecimal powerHigh;
    private BigDecimal powerLow;
    private BigDecimal powerLower;
    private BigDecimal powerDeepLow;

    /**
     * 抄表时间
     */
    @NotNull(message = "抄表时间不能为空")
    private LocalDateTime recordTime;

    /**
     * 是否计算消费
     */
    @NotNull(message = "是否计算消费不能为空")
    private Boolean needConsume;
}
