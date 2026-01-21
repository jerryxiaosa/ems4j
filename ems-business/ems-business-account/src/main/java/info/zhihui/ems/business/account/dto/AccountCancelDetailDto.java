package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户销户详情DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountCancelDetailDto {

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

    /**
     * 销表明细列表
     */
    private List<CanceledMeterDto> meterList;
}