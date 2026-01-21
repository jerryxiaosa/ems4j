package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class MeterCancelDetailDto {

    /**
     * 表id
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    /**
     * 手动传入的电表读数，当电表离线时使用
     */
    private BigDecimal powerHigher;
    private BigDecimal powerHigh;
    private BigDecimal powerLow;
    private BigDecimal powerLower;
    private BigDecimal powerDeepLow;

}
