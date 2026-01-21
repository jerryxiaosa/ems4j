package info.zhihui.ems.business.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销表信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_meter_cancel_record")
public class MeterCancelRecordEntity extends BaseEntity {

    private Integer id;

    /**
     * 销户单号
     */
    private String cancelNo;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 表类型：1电2水
     */
    private Integer meterType;

    /**
     * 表id
     */
    private Integer meterId;

    /**
     * 表名称
     */
    private String meterName;

    /**
     * 表号
     */
    private String meterNo;

    /**
     * 空间id
     */
    private Integer spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 父级id
     */
    private String spaceParentIds;

    /**
     * 父级名称
     */
    private String spaceParentNames;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否断闸
     */
    private Boolean isCutOff;

    /**
     * 余额
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
