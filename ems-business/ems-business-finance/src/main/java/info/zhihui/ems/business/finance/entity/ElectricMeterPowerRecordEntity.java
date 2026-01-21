package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电表电量记录实体
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_electric_meter_power_record")
public class ElectricMeterPowerRecordEntity {
    private Integer id;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 是否预付费
     */
    private Boolean isPrepay;

    /**
     * CT变比
     */
    private BigDecimal ct;

    /**
     * 总功率
     */
    private BigDecimal power;

    /**
     * 尖时功率
     */
    private BigDecimal powerHigher;

    /**
     * 峰时功率
     */
    private BigDecimal powerHigh;

    /**
     * 平时功率
     */
    private BigDecimal powerLow;

    /**
     * 谷时功率
     */
    private BigDecimal powerLower;

    /**
     * 深谷功率
     */
    private BigDecimal powerDeepLow;

    /**
     * 原始报告ID
     */
    private String originalReportId;

    /**
     * 抄表时间
     */
    private LocalDateTime recordTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}