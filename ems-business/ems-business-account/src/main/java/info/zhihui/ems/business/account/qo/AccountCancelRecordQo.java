package info.zhihui.ems.business.account.qo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户销户记录查询对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountCancelRecordQo {

    /**
     * 企业名称/个人名称（模糊查询）
     */
    private String ownerName;

    /**
     * 结算类型（CleanBalanceTypeEnum的code值）
     */
    private Integer cleanBalanceType;
}