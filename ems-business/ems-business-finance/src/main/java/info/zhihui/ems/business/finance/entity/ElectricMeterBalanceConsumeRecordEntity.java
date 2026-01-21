package info.zhihui.ems.business.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额消费记录实体类
 * 对应数据库表：energy_electric_meter_balance_consume_record
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@TableName("energy_electric_meter_balance_consume_record")
public class ElectricMeterBalanceConsumeRecordEntity {
    private Integer id;

    /**
     * 电表消费记录ID
     */
    private Integer meterConsumeRecordId;

    /**
     * 消费编号
     */
    private String consumeNo;

    /**
     * 消费类型
     */
    private Integer consumeType;

    /**
     * 电表类型
     */
    private Integer meterType;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 电账户类型
     */
    private Integer electricAccountType;

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
     * 归属者ID
     */
    private Integer ownerId;

    /**
     * 归属者类型
     */
    private Integer ownerType;

    /**
     * 归属者名称
     */
    private String ownerName;

    /**
     * 空间ID
     */
    private Integer spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 价格方案ID
     */
    private Integer pricePlanId;

    /**
     * 价格方案名称
     */
    private String pricePlanName;

    /**
     * 年度阶梯起始值
     */
    private BigDecimal stepStartValue;

    /**
     * 历史用电偏移（销户继承/年结数据）
     */
    private BigDecimal historyPowerOffset;

    /**
     * 阶梯倍率
     */
    private BigDecimal stepRate;

    /**
     * 消费总金额
     */
    private BigDecimal consumeAmount;

    /**
     * 尖消费金额
     */
    private BigDecimal consumeAmountHigher;

    /**
     * 峰消费金额
     */
    private BigDecimal consumeAmountHigh;

    /**
     * 平消费金额
     */
    private BigDecimal consumeAmountLow;

    /**
     * 谷消费金额
     */
    private BigDecimal consumeAmountLower;

    /**
     * 深谷消费金额
     */
    private BigDecimal consumeAmountDeepLow;

    /**
     * 尖单价
     */
    private BigDecimal priceHigher;

    /**
     * 峰单价
     */
    private BigDecimal priceHigh;

    /**
     * 平单价
     */
    private BigDecimal priceLow;

    /**
     * 谷单价
     */
    private BigDecimal priceLower;

    /**
     * 深谷单价
     */
    private BigDecimal priceDeepLow;

    /**
     * 开始余额
     */
    private BigDecimal beginBalance;

    /**
     * 结束余额
     */
    private BigDecimal endBalance;

    /**
     * 备注
     */
    private String remark;

    /**
     * 消费时间
     */
    private LocalDateTime meterConsumeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
