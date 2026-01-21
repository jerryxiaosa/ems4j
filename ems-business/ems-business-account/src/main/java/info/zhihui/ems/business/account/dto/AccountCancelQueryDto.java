package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户销户查询DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountCancelQueryDto {

    /**
     * 企业名称/个人名称（模糊查询）
     */
    private String ownerName;

    /**
     * 结算类型
     */
    private CleanBalanceTypeEnum cleanBalanceType;
}