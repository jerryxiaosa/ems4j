package info.zhihui.ems.business.device.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MeterCancelResultDto {
    private Integer meterId;
    private BigDecimal balance;
    private BigDecimal historyPowerTotal;
}
