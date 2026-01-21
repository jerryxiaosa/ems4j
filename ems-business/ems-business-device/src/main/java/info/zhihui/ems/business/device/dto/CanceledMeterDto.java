package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.MeterTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户销户表计明细DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class CanceledMeterDto {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间父级名称（逗号拼接）
     */
    private String spaceParentNames;

    /**
     * 表名称
     */
    private String meterName;

    /**
     * 表具号
     */
    private String meterNo;

    /**
     * 表类型
     */
    private MeterTypeEnum meterType;

    /**
     * 表余额
     */
    private BigDecimal balance;

    /**
     * 读数
     */
    private BigDecimal power;

    /**
     * 尖电量
     */
    private BigDecimal powerHigher;

    /**
     * 峰电量
     */
    private BigDecimal powerHigh;

    /**
     * 平电量
     */
    private BigDecimal powerLow;

    /**
     * 谷电量
     */
    private BigDecimal powerLower;

    /**
     * 深谷电量
     */
    private BigDecimal powerDeepLow;

    /**
     * 本年度阶梯累计用量（用于重开户继承）
     */
    private BigDecimal historyPowerTotal;

    /**
     * 读表时间
     */
    private LocalDateTime showTime;
}