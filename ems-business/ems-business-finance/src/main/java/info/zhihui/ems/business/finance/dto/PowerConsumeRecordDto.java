package info.zhihui.ems.business.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电量消费记录查询出参DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class PowerConsumeRecordDto {

    /**
     * 记录ID
     */
    private Integer id;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 电表ID
     */
    private Integer meterId;

    /**
     * 消费编号
     */
    private String consumeNo;

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
     * 表名称
     */
    private String meterName;

    /**
     * 表编号
     */
    private String deviceNo;

    /**
     * 房间名称
     */
    private String spaceName;

    /**
     * 消费前余额
     */
    private BigDecimal beginBalance;

    /**
     * 消费金额
     */
    private BigDecimal consumeAmount;

    /**
     * 消费后余额
     */
    private BigDecimal endBalance;

    /**
     * 是否合并计量（派生字段：electric_account_type == ElectricAccountTypeEnum.MERGED.code）
     */
    private Boolean mergedMeasure;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;
}
