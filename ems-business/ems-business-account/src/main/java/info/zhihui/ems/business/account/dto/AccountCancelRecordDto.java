package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户销户记录列表DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountCancelRecordDto {

    /**
     * 销户编号
     */
    private String cancelNo;

    /**
     * 企业名称/个人名称
     */
    private String ownerName;

    /**
     * 销表数量
     */
    private Integer electricMeterAmount;

    /**
     * 结算类型
     */
    private CleanBalanceTypeEnum cleanBalanceType;

    /**
     * 结算金额
     */
    private BigDecimal cleanBalanceReal;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 销户时间
     */
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;
}