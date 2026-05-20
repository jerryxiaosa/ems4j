package info.zhihui.ems.business.mobile.bo;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小程序当前用户与开户账户信息。
 */
@Data
@Accessors(chain = true)
public class MiniCurrentUserBo {
    private String userPhone;
    private Integer electricAccountId;
    private String electricAccountName;
    private ElectricAccountTypeEnum electricAccountType;
    private BigDecimal balance;
    private Integer meterCount;
}
