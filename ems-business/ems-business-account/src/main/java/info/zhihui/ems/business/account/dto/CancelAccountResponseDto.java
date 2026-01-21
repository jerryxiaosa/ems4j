package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class CancelAccountResponseDto {

    private String cancelNo;

    private CleanBalanceTypeEnum cleanBalanceType;

    private BigDecimal amount;
}
