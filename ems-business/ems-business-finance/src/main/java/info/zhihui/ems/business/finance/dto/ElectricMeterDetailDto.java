package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterDetailDto {

    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    /**
     * 电表名称
     */
    @NotNull(message = "电表名称不能为空")
    private String meterName;

    /**
     * 电表编号，系统生成
     */
    @NotNull(message = "电表编号不能为空")
    private String meterNo;

    /**
     * 空间id
     */
    @NotNull(message = "空间ID不能为空")
    private Integer spaceId;

    /**
     * 是否计量
     * 汇总时是否计算在内，和calculate_type无关
     */
    @NotNull(message = "是否计量不能为空")
    private Boolean isCalculate;

    /**
     * 用量类型，和is_calculate无关
     * 目前是设备品类
     */
    private CalculateTypeEnum calculateType;

    /**
     * 是否为预付费
     */
    @NotNull(message = "是否为预付费不能为空")
    private Boolean isPrepay;

    /**
     * 计费方案id
     */
    private Integer pricePlanId;

    /**
     * ct变比
     */
    private BigDecimal ct;

    /**
     * 年度阶梯起始值
     */
    private BigDecimal stepStartValue;

    /**
     * 重开户继承时需要补偿的历史阶梯用量
     */
    private BigDecimal historyPowerOffset;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否断闸
     */
    private Boolean isCutOff;
}
