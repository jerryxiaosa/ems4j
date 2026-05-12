package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 小程序当前用户与开户账户信息。
 */
@Data
@Accessors(chain = true)
public class MiniCurrentUserVo {
    private String userPhone;
    private Integer electricAccountId;
    private String electricAccountName;
    private Integer electricAccountType;
    private BigDecimal balance;
    private Integer meterCount;
}
